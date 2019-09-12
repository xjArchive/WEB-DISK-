package com.cloud.entity;

import java.util.List;

public interface FolderMapper
{
	//�����ļ���
	public void addFolder(Folder folder);
	
	//���Ӹ��ļ��У������û�ע��ʱʹ��
	public void addRootFolder(User user);
	
	//���ݸ��ļ���IDѡȡ�����ļ���
	public List<Folder> selectFolderListByFolderId(Integer folderId);
	
	/*�����û�IDѡȡ�����ļ���*/
	public Folder selectRootFolderByUserId(Integer userId);
	
	//�����ļ���ID��ȡ�ļ���
	public Folder selectFolderById(Integer folderId);
	
	//�������ļ��еĸ��ļ���ID�в��Ҹ��ļ���
	public Folder selectFatherFolderById(Integer folderId);
	
	//�����ļ���IDɾ�����ļ���
	public void deleteFolderById(Integer id);
	
	//���ݸ��ļ���ɸѡ���������е��ļ���
	public List<Folder> selectAllFolderByFatherFolder(Folder fatherFolder);
	
}
