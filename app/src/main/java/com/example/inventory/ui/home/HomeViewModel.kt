/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.ui.item.dailyProfitLoss
import com.webencyclop.core.mftool.MFTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import java.util.stream.Collectors

/**
 * ViewModel to retrieve all items in the Room database.
 */
class HomeViewModel(
    private val itemsRepository: ItemsRepository,
    private val mfTool: MFTool
) : ViewModel() {

    /**
     * Holds home ui state. The list of items are retrieved from [ItemsRepository] and mapped to
     * [HomeUiState]
     */
    private val _homeUiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    private val _homeUiState2: MutableStateFlow<HomeUiState2> = MutableStateFlow(HomeUiState2())
    private val _homeUiState3: MutableStateFlow<HomeUiStateDailyPL> = MutableStateFlow(HomeUiStateDailyPL())
    private val _homeUiState4: MutableStateFlow<HomeUiStateDailyPL2> = MutableStateFlow(HomeUiStateDailyPL2())

    val homeUiState: StateFlow<HomeUiState> = _homeUiState
    val homeUiState2: StateFlow<HomeUiState2> = _homeUiState2
    val homeUiState3: StateFlow<HomeUiStateDailyPL> = _homeUiState3
    val homeUiState4: StateFlow<HomeUiStateDailyPL2> = _homeUiState4
    init {
        fetchItemsWithLatestNav()
    }

    private fun fetchItemsWithLatestNav() {
        viewModelScope.launch(Dispatchers.IO) {
            itemsRepository.getAllItemsStream().collect { items ->
                val updatedItems = mutableListOf<Item>() // Create a mutable list to hold updated items

                for (item in items) {
                    val latestNavDeferred = async {
                        // Fetch latest NAV for each item asynchronously
                       if(LocalDate.now().isAfter(LocalDate.parse(item.navDate)) || item.latestNav<1) {
                           mfTool.getCurrentNav(item.code)
                       }else {
                           item.latestNav
                       }
                    }
                    val latestNav = latestNavDeferred.await()
                    val compare=item.latestNav.compareTo(latestNav.toDouble())
                    if(compare!=0 || LocalDate.now().isAfter(LocalDate.parse(item.navDate))){

                        var list = mfTool.historicNavForScheme(item.code).stream()
                            .sorted { data, data2 -> data2.date.compareTo(data.date) }
                            .collect(Collectors.toList());
                        var latest=list.get(0)
                        var  previous=list.get(1)

                        itemsRepository.updateItem(item.copy(latestNav = latest.nav.toDouble()).copy(navDate = LocalDate.now().toString())
                            .copy(previousNav = previous.nav.toDouble()))
                        updatedItems.add(item.copy(latestNav = latest.nav.toDouble()).copy(navDate = LocalDate.now().toString()).copy(previousNav = previous.nav.toDouble()))

                    }else {
                        updatedItems.add(item)
                    }
                }

                var sum=0.0;
                var dailyPL=0.0;
                for (abc in updatedItems){
                    sum += abc.latestNav * abc.quantity
                    dailyPL+=abc.dailyProfitLoss()
                }

                val abc1=NumberFormat.getNumberInstance().format(sum);
                val abc2=NumberFormat.getNumberInstance().format(dailyPL);

                //_homeUiState2.value=HomeUiState2(sum);
                _homeUiState2.value=HomeUiState2(abc1);
                _homeUiState3.value=HomeUiStateDailyPL(abc2)
                _homeUiState4.value=HomeUiStateDailyPL2(dailyPL)
                _homeUiState.value = HomeUiState(updatedItems)
            }
        }
    }
}

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(val itemList: List<Item> = listOf())

/*data class HomeUiState2(val total:Double=0.0)*/
data class HomeUiState2(val total:String="")
data class HomeUiStateDailyPL(val dailyPL:String="")
data class HomeUiStateDailyPL2(val dailyPL:Double=0.0)
