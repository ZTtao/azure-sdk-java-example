/*
 * Copyright (c) 2015-2020, Chen Rui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vianet.azure.sdk.manage.storage;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import static java.util.Base64.getEncoder;

/**
 * Created by chen.rui on 3/25/2016.
 */
public class RestApi {

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {

        String key = "oLbZhKoC8HaMUwjGWInitvAJG8rkh4Gmmnpo8JIUc0Vpk/8D8lxT8DpE9A0WVvXtDvjwdhR2AHkj5x5LvHturw==";
        Date date = new Date();
        Long expiry = date.getTime() + 3600;
        String stringToSign = "GET\n\napplication/json;odata=nometadata\n\n/kevinstorage1/$MetricsCapacityBlob";
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));

        String signature = getEncoder().encodeToString(hmacSha256.doFinal(stringToSign.getBytes()));

        String signatureHeader = "SharedKey kevinstorage1:"+signature;
        System.out.println(signatureHeader);

    }

}
