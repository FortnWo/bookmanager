package com.fortn.bookmanager.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(value = "username") // 指定主键字段
    private String username;
    private String password;
    private String role;
    // 其它字段...
}
