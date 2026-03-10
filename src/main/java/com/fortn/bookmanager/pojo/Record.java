package com.fortn.bookmanager.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("lend_list")
public class Record {
    @TableId(value = "sernum", type = IdType.AUTO)
    private Long sernum;
    private Long bookId;
    private String bookName;
    private Long readerId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lendDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date backDate;
}
