package com.example.myapplication.data

class ClosetRepository(
    private val dao: ClosetDao
) {
    fun getItems() = dao.getAll()

    suspend fun addItem(item: ClosetItemEntity) {
        dao.insert(item)
    }
}