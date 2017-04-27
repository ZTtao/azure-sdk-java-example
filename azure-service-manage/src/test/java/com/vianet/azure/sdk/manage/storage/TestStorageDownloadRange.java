package com.vianet.azure.sdk.manage.storage;


import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.vianet.azure.sdk.manage.Configure;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class TestStorageDownloadRange {


    @Test
    public void testDownLoadRange() throws Exception {
        Configure configure = new Configure();
        CloudBlobClient client = AzureStorageServiceFactory.getInstance(configure).createBlobService().getClient();
        CloudBlobContainer container = client.getContainerReference("files");
        CloudBlockBlob cloudBlockBlob = container.getBlockBlobReference("ts1");
        final OutputStream os = new FileOutputStream("F:\\Documents\\workspace\\azure-sdk-java-example\\azure-service-manage\\src\\test\\resources\\out.png");
        cloudBlockBlob.downloadRange(200, 500000L, os);
    }

}
