package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by xxtang on 2017/12/23.
 */
@Service("iUserService")
public class IUserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {

        int resultCount=userMapper.checkUsername(username);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //密码登录MD5
        String MD5Password=MD5Util.MD5EncodeUtf8(password);

        User user=userMapper.selectLogin(username,MD5Password);
        if(user==null){
            return ServerResponse.createByErrorMessage("密码不正确");
        }
        //将密码置空便于返回
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);

    }

    @Override
    public ServerResponse<String> register(User user) {
          ServerResponse validResponse=this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }

        validResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount=userMapper.insert(user);

        if(resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");

    }

    public ServerResponse<String> checkValid(String str,String type){
        if(StringUtils.isNotBlank(type)){
            //开始校验
            if(type.equals(Const.USERNAME)){
                int resultCount=userMapper.checkUsername(str);

                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }

            }

            if(type.equals(Const.EMAIL)){
               int resultCount=userMapper.checkEmail(str);

                if(resultCount>0){
                    return  ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse selectQuestion(String username){
          ServerResponse validResponse=this.checkValid(username,Const.USERNAME);

        if(validResponse.isSuccess()){
            //用户名不存在
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String question=userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccessMessage(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题为空");
    }

    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        int resultCount=userMapper.forgetCheckAnswer(username,question,answer);

        if(resultCount>0){
            String forgetToken= UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }

        ServerResponse validResponse=this.checkValid(username,Const.USERNAME);

        if(validResponse.isSuccess()){
            //用户名不存在
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);

         if(StringUtils.isBlank(token)){
         return ServerResponse.createByErrorMessage("TOKEN无效或者过期");
         }

        if(StringUtils.equals(token,forgetToken)){
            String md5Password=MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount=userMapper.updatePasswordByUsername(username,md5Password);
            if(rowCount>0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }

        }else{
            return ServerResponse.createByErrorMessage("token错误,请重新获取");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }


    public ServerResponse<String>  resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权,要校验这个用户的密码
        int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));

        int updatecount=userMapper.updateByPrimaryKeySelective(user);

        if(updatecount>0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> updateInformation(User user){
        //username不能被更新
        //校验新的email是不是存在的,如果存在的话不能为当前用户的
         int resultCount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return ServerResponse.createByErrorMessage("email已经存在,请更换email后再更新");
        }
        User updateUser=new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount=userMapper.updateByPrimaryKeySelective(updateUser);

        if(updateCount>0){
            return ServerResponse.createBySuccessMessage("更新个人信息成功");
        }
         return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServerResponse<User> getInformation(Integer userId){
        User user=userMapper.selectByPrimaryKey(userId);
        if(user==null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    //backend
    //判断是否为管理员

    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue()==Const.Role.ROLE_ADMIN){
          return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
    }

