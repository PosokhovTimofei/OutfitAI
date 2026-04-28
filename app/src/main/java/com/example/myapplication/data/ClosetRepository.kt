package com.example.myapplication.data

class ClosetRepository(
    private val dao: ClosetDao
) {

    // ================= ITEMS =================
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


    // ================= OUTFITS =================
    fun getOutfits() = dao.getOutfits()

    suspend fun addOutfit(outfit: OutfitEntity) {
        dao.insertOutfit(outfit)
    }

    suspend fun deleteOutfit(outfit: OutfitEntity) {
        dao.deleteOutfit(outfit)
    }

    suspend fun updateOutfit(outfit: OutfitEntity){
        dao.updateOutfit(outfit)
    }
}