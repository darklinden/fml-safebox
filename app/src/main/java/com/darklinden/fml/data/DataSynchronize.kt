package com.darklinden.fml.data

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.*
import com.darklinden.fml.Application
import com.darklinden.fml.config.Config
import java.io.File
import java.io.IOException
import java.util.*
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest
import com.alibaba.sdk.android.oss.model.DeleteObjectResult


object DataSynchronize {

    interface Callback {
        fun onFinished()
    }

    class TestListObjectsCallback : OSSCompletedCallback<ListObjectsRequest, ListObjectsResult> {

        var request: ListObjectsRequest? = null
        var result: ListObjectsResult? = null
        var clientException: ClientException? = null
        var serviceException: ServiceException? = null

        override fun onSuccess(request: ListObjectsRequest, result: ListObjectsResult) {
            this.request = request
            this.result = result
        }

        override fun onFailure(
            request: ListObjectsRequest,
            clientExcepion: ClientException,
            serviceException: ServiceException
        ) {
            this.request = request
            this.clientException = clientExcepion
            this.serviceException = serviceException
        }
    }

    var isSyncRunning: Boolean = false

    private var oss: OSSClient? = null

    private fun getOss(): OSSClient {

        if (oss != null) {
            return oss!!
        }

        //if null , default will be init
        val conf = ClientConfiguration()
        conf.connectionTimeout = 15 * 1000 // connction time out default 15s
        conf.socketTimeout = 15 * 1000 // socket timeout，default 15s
        conf.maxConcurrentRequest = 5 // synchronous request number，default 5
        conf.maxErrorRetry = 2 // retry，default 2
//        OSSLog.enableLog() //write local log file ,path is SDCard_path\OSSLog\logs.csv

        val credentialProvider = OSSStsTokenCredentialProvider(
            Config.accessKeyId,
            Config.accessKeySecret,
            ""
        )

        oss = OSSClient(Application.app, Config.endpoint, credentialProvider, conf)
        return oss!!
    }

    fun sync(finished: Callback?) {
        if (isSyncRunning) {
            Toast.makeText(
                Application.app,
                com.darklinden.fml.R.string.toast_sync_running,
                Toast.LENGTH_LONG
            ).show()
            finished?.onFinished()
            return
        }

        isSyncRunning = true
        Handler().postDelayed({

            val oss = getOss()

            val listObjects = ListObjectsRequest(Config.bucketName)

            val callback = TestListObjectsCallback()

            val task = oss.asyncListObjects(listObjects, callback)

            task.waitUntilFinished()

            val sums = callback.result?.objectSummaries

            val xfiles = mutableListOf<String>()

            if (sums?.size ?: 0 > 0) {
                for (s in sums!!) {
                    Log.d("", "\t" + s.key)

                    val remoteModify = s.lastModified.time

                    var localModify: Long = 0

                    try {
                        localModify = File(Application.app.filesDir, s.key).lastModified()
                    } catch (e: Exception) {
                        localModify = 0
                        e.printStackTrace()
                    }

                    if (remoteModify > localModify) {
                        xfiles.add(s.key)

                        // Construct an object download request
                        val get = GetObjectRequest(Config.bucketName, s.key)

                        val task = oss.asyncGetObject(
                            get,
                            object : OSSCompletedCallback<GetObjectRequest, GetObjectResult> {
                                override fun onSuccess(
                                    request: GetObjectRequest,
                                    result: GetObjectResult
                                ) {
                                    // Request succeeds
                                    Log.d("Content-Length", "" + result.contentLength)

                                    val inputStream = result.objectContent

                                    try {

                                        val out = Application.app.openFileOutput(
                                            s.key,
                                            Context.MODE_PRIVATE
                                        )
                                        out.write(inputStream.readBytes())
                                        out.close()

                                        File(
                                            Application.app.filesDir,
                                            s.key
                                        ).setLastModified(s.lastModified.time)

                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }

                                override fun onFailure(
                                    request: GetObjectRequest,
                                    clientExcepion: ClientException?,
                                    serviceException: ServiceException?
                                ) {
                                    // Request exception
                                    clientExcepion?.printStackTrace()
                                    if (serviceException != null) {
                                        // Service exception
                                        Log.e("ErrorCode", serviceException.errorCode)
                                        Log.e("RequestId", serviceException.requestId)
                                        Log.e("HostId", serviceException.hostId)
                                        Log.e("RawMessage", serviceException.rawMessage)
                                    }
                                }
                            })

                        // task.cancel(); // Cancel the task

                        task.waitUntilFinished(); // Wait till the task is finished as needed

                    } else if (remoteModify < localModify) {
                        xfiles.add(s.key)

                        val m = ObjectMetadata()
                        m.lastModified = Date(localModify)
                        // Construct an upload request
                        val put =
                            PutObjectRequest(
                                Config.bucketName,
                                s.key,
                                File(Application.app.filesDir, s.key).absolutePath,
                                m
                            )

                        // You can set progress callback during asynchronous upload
                        put.progressCallback =
                            OSSProgressCallback { request, currentSize, totalSize ->
                                Log.d(
                                    "PutObject",
                                    "currentSize: $currentSize totalSize: $totalSize"
                                )
                            }

                        val task = oss.asyncPutObject(
                            put,
                            object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                                override fun onSuccess(
                                    request: PutObjectRequest,
                                    result: PutObjectResult
                                ) {
                                    Log.d("PutObject", "UploadSuccess")
                                }

                                override fun onFailure(
                                    request: PutObjectRequest,
                                    clientExcepion: ClientException?,
                                    serviceException: ServiceException?
                                ) {
                                    // Request exception
                                    clientExcepion?.printStackTrace()
                                    if (serviceException != null) {
                                        // Service exception
                                        Log.e("ErrorCode", serviceException.errorCode)
                                        Log.e("RequestId", serviceException.requestId)
                                        Log.e("HostId", serviceException.hostId)
                                        Log.e("RawMessage", serviceException.rawMessage)
                                    }
                                }
                            })

                        // task.cancel(); // Cancel the task

                        task.waitUntilFinished(); // Wait till the task is finished
                    } else {
                        xfiles.add(s.key)
                    }
                }
            }

            val allFile = Application.app.fileList()
            for (f in allFile) {
                if (f in xfiles) {
                    continue
                }

                val file = File(Application.app.filesDir, f)
                val m = ObjectMetadata()
                m.lastModified = Date(file.lastModified())
                // Construct an upload request
                val put =
                    PutObjectRequest(
                        Config.bucketName,
                        f,
                        file.absolutePath,
                        m
                    )

                // You can set progress callback during asynchronous upload
                put.progressCallback =
                    OSSProgressCallback { request, currentSize, totalSize ->
                        Log.d(
                            "PutObject",
                            "currentSize: $currentSize totalSize: $totalSize"
                        )
                    }

                val task = oss.asyncPutObject(
                    put,
                    object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                        override fun onSuccess(
                            request: PutObjectRequest,
                            result: PutObjectResult
                        ) {
                            Log.d("PutObject", "UploadSuccess")
                        }

                        override fun onFailure(
                            request: PutObjectRequest,
                            clientExcepion: ClientException?,
                            serviceException: ServiceException?
                        ) {
                            // Request exception
                            clientExcepion?.printStackTrace()
                            if (serviceException != null) {
                                // Service exception
                                Log.e("ErrorCode", serviceException.errorCode)
                                Log.e("RequestId", serviceException.requestId)
                                Log.e("HostId", serviceException.hostId)
                                Log.e("RawMessage", serviceException.rawMessage)
                            }
                        }
                    })

                // task.cancel(); // Cancel the task

                task.waitUntilFinished(); // Wait till the task is finished
            }

            isSyncRunning = false

            finished?.onFinished()

        }, 100)
    }

    fun rm(f: String, finished: Callback?) {

        if (isSyncRunning) {
            Toast.makeText(
                Application.app,
                com.darklinden.fml.R.string.toast_sync_running,
                Toast.LENGTH_LONG
            ).show()
            finished?.onFinished()
            return
        }

        isSyncRunning = true
        Handler().postDelayed({

            val oss = getOss()

            // Create a delete request
            val delete = DeleteObjectRequest(Config.bucketName, f)
            val deleteTask = oss.asyncDeleteObject(
                delete,
                object : OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult> {
                    override fun onSuccess(
                        request: DeleteObjectRequest,
                        result: DeleteObjectResult
                    ) {
                        Log.d("asyncCopyAndDelObject", "success!")
                    }

                    override fun onFailure(
                        request: DeleteObjectRequest,
                        clientExcepion: ClientException?,
                        serviceException: ServiceException?
                    ) {
                        // Handle the exceptions returned for the request.
                        clientExcepion?.printStackTrace()
                        if (serviceException != null) {
                            // A service exception occurs.
                            Log.e("ErrorCode", serviceException.errorCode)
                            Log.e("RequestId", serviceException.requestId)
                            Log.e("HostId", serviceException.hostId)
                            Log.e("RawMessage", serviceException.rawMessage)
                        }
                    }
                })

            deleteTask.waitUntilFinished()

            isSyncRunning = false

            finished?.onFinished()

        }, 100)
    }
}