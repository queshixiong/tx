package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by xxtang on 2018/1/22.
 */
public interface IFileService {
    public String upload(MultipartFile file, String path);
}
