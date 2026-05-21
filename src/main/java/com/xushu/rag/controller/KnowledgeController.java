package com.xushu.rag.controller;

/**
 * @Title: KnowledgeController
 * @Author Xushu
 * @Package com.Xushu.rag.controller
 * @Date 2025/2/8 20:35
 * @description: 知识库
 */

import com.alibaba.fastjson2.JSON;
import com.xushu.rag.common.*;
import com.xushu.rag.context.BaseContext;
import com.xushu.rag.entity.AliOssFile;
import com.xushu.rag.pojo.dto.QueryFileDTO;
import com.xushu.rag.service.AliOssFileService;
import com.xushu.rag.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "KnowledgeController", description = "知识库管理接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/knowledge")
public class KnowledgeController {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private TokenTextSplitter tokenTextSplitter;



    @Autowired
    private AliOssFileService aliOssFileService;
    /**
     * 上传附件接口
     *
     *  1. 提供不同的分片策略
     *  2. 分片后的预览
     * @param
     * @return
     * @throws IOException
     */

    @Operation(summary = "upload", description = "上传附件接口")
    @PostMapping(value = "file/upload", headers = "content-type=multipart/form-data")
    public BaseResponse upload(@RequestParam("file") List<MultipartFile> files) {
        try {
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                // 上传文件到OSS
                String ossUrl = aliOssUtil.upload(file.getBytes(), fileName);

                Resource resource = file.getResource();
                TikaDocumentReader documentReader = new TikaDocumentReader(resource);
                List<Document> documents = documentReader.get();

                // 使用TokenTextSplitter将文档分割成小块

                //上传知识库的时候需要需要 选择分块策略

                //1、固定的分块策略 ，每块多少个token，为了不割裂语义，建议两个快之间要有一部分的重叠
                //2、按找语义进行分割：就是将文档进行细分后，从第一块开始，和第二个快对比相似度如果相似就合并为一个，然后拿到第3个再次进行计算，直到不相似在重新开快。要找到那个阈值
                //3、递归分块：先将文档按照分隔符进行分块，如：句号等，然后如果超出了一个快的大小，在将这个快进行同样的方式分割
                //4、结构化分块策略 ，根据结构进行分块，比如根据标题、章节、段落等进行分块，但是并不是所有的文档都是规整的
                //5、LLM分分快策略，将文档直接丢给大模型，让其分块

                //


                List<Document> splitDocuments = tokenTextSplitter.apply(documents);
                for (Document splitDocument : splitDocuments) {
                    splitDocument.getMetadata().put(KnowledgeEnum.TYPE.getType(), DocumentEnum.DOCUMENT.getName() );
                    splitDocument.getMetadata().put(KnowledgeEnum.USER_ID.getType(), BaseContext.getCurrentId());
                }
                // 将分割后的文档添加到向量数据库
                vectorStore.add(splitDocuments);

                // 收集所有向量ID
                List<String> vectorIds = splitDocuments.stream()
                        .map(doc -> doc.getId())
                        .collect(Collectors.toList());

                // 保存文件信息到数据库
                AliOssFile aliOssFile = AliOssFile.builder()
                        .fileName(fileName)
                        .url(ossUrl)
                        .vectorId(JSON.toJSONString(vectorIds))
                        .createTime(new Date())
                        .updateTime(new Date())
                        .build();
                aliOssFileService.save(aliOssFile);

            }
        } catch (Exception e) {
            log.error("文件上传失败: ", e);
            return ResultUtils.error("上传失败");

        }
        return ResultUtils.success("文件上传成功");
    }


    @Operation(summary = "contents",description = "文件查询")
    @GetMapping("/contents")
    public BaseResponse queryFiles(QueryFileDTO request){
        if(request.getPage() == null || request.getPageSize() == null){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,"page 或 pageSize为空");
        }
        return aliOssFileService.queryPage(request);
    }

    @Operation(summary = "delete",description = "文件删除")
    @DeleteMapping("/delete")
    public BaseResponse deleteFiles(@RequestParam List<Long> ids){
        return aliOssFileService.deleteFiles(ids);
    }


    @Operation(summary = "download",description = "文件下载")
    @GetMapping("/download")
    public BaseResponse downloadFiles(@RequestParam List<Long> ids){
        return aliOssFileService.downloadFiles(ids);
    }





}
