package com.example.myapplication.data

class ClosetRepository(
    private val dao: ClosetDao
) {
    fun getItems() = dao.getAll()

    suspend fun addItem(item: ClosetItemEntity) {
        dao.insert(item)
    }
    suspend fun deleteItem(item: ClosetItemEntity) {
        dao.delete(item)
    }

    suspend fun updateItem(item: ClosetItemEntity) {
        dao.updateItem(item)
    }
}