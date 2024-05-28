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

package com.example.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Database class with a singleton Instance object.
 */
@Database(entities = [Item::class], version = 14, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var Instance: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, InventoryDatabase::class.java, "item_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                Instance = instance
                instance

            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Insert default data here
            CoroutineScope(Dispatchers.IO).launch {
                val dao = Instance?.itemDao()

//
                //dao?.deleteAll()
                dao?.insert(Item(6,"Axis Bluechip",111.1,1238.43,"120465",0.0))
                dao?.insert(Item(7,"Axis Gold",111.1,2507.43,"120473",0.0))
                dao?.insert(Item(8,"Quant MidCap",111.1,482.161,"120841",0.0))
                dao?.insert(Item(9,"Quant Active",111.1,144.667,"120823",0.0))
                dao?.insert(Item(10,"Quant Small",111.1,100.681,"120828",0.0))
                dao?.insert(Item(11,"Axis Midcap",111.1,1304.073,"120505",0.0))
                dao?.insert(Item(12,"ICICI Bluechip",111.1,879.183,"120586",0.0))
                dao?.insert(Item(1,"S&P 500",111.1,10731.2901,"148381",0.0))
                dao?.insert(Item(2,"HDFC SmallCap",111.1,239.0,"130503",0.0))
                dao?.insert(Item(3,"Axis SmallCap",111.1,507.349,"125354",0.0))
                dao?.insert(Item(4,"SBI SmallCap",111.1,825.693,"125497",0.0))
                dao?.insert(Item(5,"Mirae Bluechip",111.1,783.7409,"118834",0.0))


            }

        }
    }
}



