package com.cloud.entity;

public interface UserMapper 
{
	//�½��û�
	public void addUser(User user);
	
	//ͨ���û��������û�
	public User selectUserByUsername(String username);
	
	//�����û�״̬
	public void changeUserStatus(Integer userId);
}
