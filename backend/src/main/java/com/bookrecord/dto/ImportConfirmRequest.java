package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导入确认请求DTO
 * 用于用户确认导入时的请求数据
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportConfirmRequest {

    /**
     * 文件来源类型（douban/weread）
     */
    private String sourceType;

    /**
     * 重复书籍处理策略（skip/overwrite）
     * skip: 跳过重复书籍
     * overwrite: 覆盖已有书籍信息
     */
    private String duplicateStrategy;

    /**
     * 要导入的书籍索引列表（从预览列表中选择）
     * 如果为空则导入全部
     */
    private List<Integer> selectedIndexes;
}