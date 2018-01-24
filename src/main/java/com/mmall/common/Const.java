package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by xxtang on 2017/12/23.
 */
public class Const {
    public static final String CURRENT_USER="currentUser";

    public static final String USERNAME="username";

    public static final String EMAIL="email";

    public interface Role{
        int ROLE_CUSTOMER=0;//普通用户
        int ROLE_ADMIN=1;//管理员
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }
}
