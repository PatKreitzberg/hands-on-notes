package com.wyldsoft.notes.data.repository

import com.wyldsoft.notes.data.database.dao.FolderDao
import com.wyldsoft.notes.data.database.entities.FolderEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface FolderRepository {
    suspend fun getFolder(folderId: String): FolderEntity?
    fun getSubfolders(parentId: String?): Flow<List<FolderEntity>>
    suspend fun getRootFolder(): FolderEntity
    suspend fun createFolder(name: String, parentId: String): FolderEntity
    suspend fun renameFolder(folderId: String, newName: String)
    suspend fun deleteFolder(folderId: String)
    suspend fun getFolderPath(folderId: String): List<FolderEntity>
}

class FolderRepositoryImpl(
    private val folderDao: FolderDao
) : FolderRepository {
    
    override suspend fun getFolder(folderId: String): FolderEntity? {
        return folderDao.getFolder(folderId)
    }
    
    override fun getSubfolders(parentId: String?): Flow<List<FolderEntity>> {
        return folderDao.getSubfolders(parentId)
    }
    
    override suspend fun getRootFolder(): FolderEntity {
        return folderDao.getRootFolder() ?: throw IllegalStateException("Root folder not found")
    }
    
    override suspend fun createFolder(name: String, parentId: String): FolderEntity {
        val folder = FolderEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            parentFolderId = parentId
        )
        folderDao.insert(folder)
        return folder
    }
    
    override suspend fun renameFolder(folderId: String, newName: String) {
        val folder = folderDao.getFolder(folderId) ?: return
        folderDao.update(
            folder.copy(
                name = newName,
                modifiedAt = System.currentTimeMillis()
            )
        )
    }
    
    override suspend fun deleteFolder(folderId: String) {
        folderDao.deleteById(folderId)
    }
    
    override suspend fun getFolderPath(folderId: String): List<FolderEntity> {
        return folderDao.getFolderPath(folderId)
    }
}