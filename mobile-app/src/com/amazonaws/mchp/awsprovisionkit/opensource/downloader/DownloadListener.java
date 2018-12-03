/*
 * Copyright (C) 2013 Snowdream Mobile <yanghui1986527@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.mchp.awsprovisionkit.opensource.downloader;

import com.amazonaws.mchp.awsprovisionkit.opensource.downloader.asyn.TaskListener;


/**
 * @author snowdream <yanghui1986527@gmail.com>
 * @version v1.0
 * @date Sep 29, 2013
 */
@SuppressWarnings("hiding")
public class DownloadListener<Integer, DownloadTask> extends TaskListener<Integer, DownloadTask> {
    /**
     * The download task has been added to the sqlite.
     * <p/>
     * operation of UI allowed.
     *
     * @param task the download task which has been added to the sqlite.
     */
    public void onAdd(DownloadTask task) {
    }

    /**
     * The download task has been delete from the sqlite
     * <p/>
     * operation of UI allowed.
     * @param task the download task which has been deleted to the sqlite.
     */
    public void onDelete(DownloadTask task) {
    }

    /**
     * The download task is stop
     * <p/>
     * operation of UI allowed.
     * @param task the download task which has been stopped.
     */
    public void onStop(DownloadTask task) {
    }
}
