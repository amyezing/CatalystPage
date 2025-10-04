package catalystpage.com.util

import catalystpage.com.db.EnvConfig
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import java.io.FileInputStream
import java.net.URLConnection

object GcsService {
    private val storage: Storage
    private val bucketName = EnvConfig.gcsBucketName

    init {
        val credentialsPath = EnvConfig.gcsCredentialsPath
        val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsPath))
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().service
    }

    fun uploadFile(fileName: String, bytes: ByteArray): String {
        val blobId = BlobId.of(bucketName, "products/$fileName")
        val contentType = URLConnection.guessContentTypeFromName(fileName) ?: "application/octet-stream"
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(contentType)
            .build()
        storage.create(blobInfo, bytes)
        return "https://storage.googleapis.com/$bucketName/products/$fileName"
    }

    fun uploadProof(fileName: String, bytes: ByteArray): String {
        // Adds "upload/" folder in the bucket
        val blobId = BlobId.of(bucketName, "upload/$fileName")

        val contentType = URLConnection.guessContentTypeFromName(fileName)
            ?: "application/octet-stream"

        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(contentType)
            .build()

        storage.create(blobInfo, bytes)

        // Returns the public URL
        return "https://storage.googleapis.com/$bucketName/upload/$fileName"
    }


}