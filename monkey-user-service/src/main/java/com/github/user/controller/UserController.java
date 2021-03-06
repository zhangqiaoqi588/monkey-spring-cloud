package com.github.user.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.github.common.base.Response;
import com.github.common.base.ResponseData;
import com.github.common.util.JWTUtils;
import com.github.user.dto.UserDto;
import com.github.user.dto.UserLoginDto;
import com.github.user.param.LoginParam;
import com.github.user.po.User;
import com.github.user.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="用户控制器")
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	public UserService userService;
	
	@Autowired
	private HttpServletRequest request;
	    
	@CreateCache(name="logoutCache:", expire = 1000)
	private Cache<String, Long> logoutCache;
	
	@ApiOperation(value = "用户登录")
	@ApiResponses({ @ApiResponse(code = 200, message = "OK", response = UserLoginDto.class) })
	@PostMapping("/login")
	public ResponseData<UserLoginDto> login(@ApiParam(value = "登录参数", required = true) @RequestBody LoginParam param) {
		if (param == null) {
			return Response.failByParams("参数不能为空");
		}
		if (!StringUtils.hasText(param.getUsername())) {
			return Response.failByParams("username不能为空");
		}
		if (!StringUtils.hasText(param.getPass())) {
			return Response.failByParams("pass不能为空");
		}
		User user = userService.login(param);
		if (user == null) {
			return Response.failByParams("用户名或者密码错误");
		}
		String token = JWTUtils.getInstance().getToken(user.getId().toString(), 60);
		UserLoginDto loginDto = UserLoginDto.builder().id(user.getId())
				.username(user.getUsername()).nickname(user.getNickname()).token(token).build();
		return Response.ok(loginDto);
	}
	
	@PostMapping("/logout")
	public ResponseData<Boolean> logout() {
		String uid = request.getHeader("uid");
		logoutCache.put(uid, 0L);
		return Response.ok(true);
	}


	List<Integer> datas = Collections.synchronizedList(new ArrayList<>());
	
	@ApiOperation(value = "获取用户信息")
	@ApiResponses({ @ApiResponse(code = 200, message = "OK", response = UserDto.class) })
	@GetMapping("/get")
	public ResponseData<UserDto> getUser(Long id) {
		String uid = request.getHeader("uid");
		System.err.println(uid);
		if (StringUtils.hasText(uid))
			datas.add(Integer.parseInt(uid));
		
		Collections.sort(datas, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});
		System.out.println(datas.toString());
		if (id == null) {
			return Response.failByParams("id不能为空");
		}
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.ok(userService.getUser(id));
	}
}
