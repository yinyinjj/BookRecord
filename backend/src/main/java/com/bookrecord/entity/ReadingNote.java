package com.bookrecord.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 读书感悟实体类
 * 表示用户对书籍的阅读感悟、总结、疑问或洞察记录
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>支持富文本 HTML 格式的感悟内容</li>
 *   <li>可关联金句创建延伸感悟</li>
 *   <li>支持标签分类和私密设置</li>
 * </ul>
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Entity
@Table(name = "reading_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingNote {

    /**
     * 感悟唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的书籍
     * 感悟必须属于某一本书
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * 关联的金句（可选）
     * 如果感悟是针对某条金句的延伸思考，则关联该金句
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    /**
     * 章节信息（可选）
     * 记录感悟所属的书籍章节
     */
    @Column(length = 100)
    private String chapter;

    /**
     * 页码（可选）
     * 记录感悟对应的书籍页码
     */
    @Column(name = "page_number")
    private Integer pageNumber;

    /**
     * 感悟标题（可选）
     * 为感悟添加一个简短的标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 感悟内容
     * 存储富文本 HTML 格式的感悟内容
     * 支持段落、标题、列表、表格、图片等格式
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 感悟类型
     * 包括：感悟(THOUGHT)、总结(SUMMARY)、疑问(QUESTION)、洞察(INSIGHT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 20)
    @Builder.Default
    private NoteType noteType = NoteType.THOUGHT;

    /**
     * 标签（可选）
     * 多个标签用逗号分隔，用于分类和检索
     */
    @Column(length = 500)
    private String tags;

    /**
     * 是否私密
     * 私密感悟仅自己可见，不可分享
     */
    @Column(name = "is_private", nullable = false)
    @Builder.Default
    private Boolean isPrivate = false;

    /**
     * 创建时间
     * 自动记录感悟首次创建的时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 自动记录感悟最后修改的时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 感悟类型枚举
     * 定义感悟的不同类型
     */
    public enum NoteType {
        /** 感悟：阅读过程中的心得体会 */
        THOUGHT,
        /** 总结：对章节或全书的内容概括 */
        SUMMARY,
        /** 疑问：阅读过程中产生的问题 */
        QUESTION,
        /** 洞察：深层次的思考和发现 */
        INSIGHT
    }
}