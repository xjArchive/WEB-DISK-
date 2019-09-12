package com.cloud.controller.file;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.entity.CloudFile;
import com.cloud.entity.CloudFileMapper;
import com.cloud.entity.Folder;
import com.cloud.entity.FolderMapper;
import com.cloud.entity.User;
import com.post.util.MyBatisUtil;
import com.post.util.SystemConstant;

@Controller
@RequestMapping(value="/main")
public class FileManage 
{
	@RequestMapping(value="/upload",method=RequestMethod.POST)
	public String FileUpload(@RequestParam("dataList")MultipartFile[] fileList, 
			@RequestParam("path") Integer folderId, Model model, HttpSession session)
	{
		String sysMsg = "";
		if(fileList.length == 0)
		{
			model.addAttribute("��ѡ���ļ�");
			return "/main/file?dir=" + folderId;
		}
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
		try
		{
			User user = (User)session.getAttribute("user");
			
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			Folder folder;
			if(folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);
			String uploadPath = SystemConstant.USER_FILE_PATH +folder.getPath() + "\\" + folder.getName();
			System.out.println(uploadPath);   //D:/images/\\qwertyuiop
			for(MultipartFile files : fileList)
			{
				String name = files.getOriginalFilename();
				int size = (int)(files.getSize() / 1024);  //�ļ���С��Ĭ����ʾ��С1kb
				if(size == 0)
					size = 1;
				
				int sub = files.getOriginalFilename().lastIndexOf('.');

				Map<String,Object> params = new HashMap<>();  //��װ�ļ���Ϣ
				params.put("fileName", name);
				params.put("userId", user.getId());
				params.put("dirId", folder.getId());
				
				CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);
				
				Integer sameNamefile = cloudFileMapper.selectFileByFileName(params).size();

				if(sameNamefile > 0)
				{
					name += ("(" + sameNamefile + ")");
				}
				CloudFile cloudFile = new CloudFile(name, size, 
						files.getOriginalFilename().substring(sub + 1).toLowerCase(), folder, user);
				
				cloudFileMapper.addFile(cloudFile);
				File uploadFile = new File(uploadPath, name);
				files.transferTo(uploadFile);
			}
			sqlSession.commit();
			sysMsg = "�ļ��ϴ��ɹ�";
			
		}
		catch(Exception e)
		{
			sysMsg = "��ѡ���ļ�";
			sqlSession.rollback();
			e.printStackTrace();
		}
		finally
		{
			model.addAttribute("sysMsg",sysMsg);
			sqlSession.close();
		}
	
		return "/main/file?dir=" + folderId;
	}
	
	@RequestMapping(value="/folderAdd")
	public String addFolder(@RequestParam("folderName")String folderName,@RequestParam("path")String path,
			HttpSession session, HttpServletRequest request,Model model)
	{
		if(folderName == null || folderName.equals(""))
		{
			model.addAttribute("sysMsg", "�������ļ�����");
			return "/main/file?dir=0";
		}
		if(!SystemConstant.isValidFileName(folderName))
		{
			model.addAttribute("sysMsg", "�ļ��������淶������������");
			return "/main/file?dir=0";
		}
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
		String sysMsg = "";
		Integer folderId = Integer.valueOf(path);
		try
		{
			User user = (User)session.getAttribute("user");
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			
			Folder fatherFolder, newFolder;
			if(folderId == 0)
				fatherFolder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				fatherFolder = folderMapper.selectFolderById(folderId);		
			
			newFolder = new Folder(folderName, fatherFolder.getId(), user, fatherFolder.getPath() 
					+ fatherFolder.getName() + "\\");
			
			String realPath = SystemConstant.USER_FILE_PATH;
			File realFolder = new File(realPath + fatherFolder.getPath() + fatherFolder.getName() 
				+ "\\" + folderName);
			if(!realFolder.exists())
				realFolder.mkdirs();
				
			folderMapper.addFolder(newFolder);
			sqlSession.commit();
			sysMsg = "�ϴ��ɹ�";
		}
		catch(Exception e)
		{
			sysMsg = "ϵͳ�쳣";
			e.printStackTrace();
		}
		finally
		{
			model.addAttribute("sysMsg",sysMsg);
			sqlSession.close();
		}
		
		return "/main/file?dir=" + folderId;
	}
	
	@RequestMapping(value="/download",method=RequestMethod.GET)
	public ResponseEntity<byte[]> fileDownload(@RequestParam("file")Integer fileId,
			@RequestParam("folder")Integer folderId, HttpSession session)
	{
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
		ResponseEntity<byte[]> responseEntity = null;
		try
		{
			User user = (User)session.getAttribute("user");
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);
			
			Folder folder;
			if(folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);
			CloudFile cloudFile = cloudFileMapper.selectFileById(fileId);
			if(cloudFile.getUploadUser().getId() != user.getId() && cloudFile.getStatus() == CloudFile.STATUS_PRIVATE)
			{
				sqlSession.close();
				return null;
			}
			
			String path = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + "\\" + cloudFile.getName();
			
			File file = new File(path);
			HttpHeaders headers = new HttpHeaders();
			String fileName = new String(cloudFile.getName().getBytes("UTF-8"),"ISO-8859-1");
			headers.setContentDispositionFormData("attachment", fileName);  
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			responseEntity = new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers,HttpStatus.CREATED);
		}
		catch(Exception e)
		{
			sqlSession.rollback();
			e.printStackTrace();
		}
		finally
		{
			sqlSession.close();
		}
		return responseEntity;
	}
	
	@RequestMapping(value="/delete",method=RequestMethod.GET)
	public String fileDelete(@RequestParam("file")Integer fileId,@RequestParam("folder")Integer folderId,
			HttpSession session, Model model)
	{
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
		String sysMsg = "";
		try
		{
			User user = (User)session.getAttribute("user");
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);
			
			Folder folder;
			if(folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);
			CloudFile cloudFile = cloudFileMapper.selectFileById(fileId);
			
			String path = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + 
					"\\" + cloudFile.getName();
			
			File file = new File(path);
			if(file.exists())
				file.delete();
			cloudFileMapper.deleteFileById(fileId);
			sqlSession.commit();
			sysMsg = "ɾ���ɹ�";
		}
		catch(Exception e)
		{
			sqlSession.rollback();
			sysMsg = "ϵͳ�쳣";
			e.printStackTrace();
		}
		finally
		{
			model.addAttribute("sysMsg", sysMsg);
			sqlSession.close();
		}
		
		return "/main/file?dir=" + folderId;
	}
	
	@RequestMapping(value="/deleteDir", method=RequestMethod.GET)
	public String folderDelete(@RequestParam("folder") Integer folderId,Model model, HttpSession session)
	{
		User user = (User)session.getAttribute("user");
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
		String sysMsg = "";
		try
		{
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);
			
			Folder folder = folderMapper.selectFolderById(folderId);
			if(folder.getCreateUser().getId() != user.getId())
			{
				model.addAttribute("sysMsg", "��û��Ȩ��ɾ�����ļ���");
				sqlSession.close();
				return "/index.jsp";
			}
			File target = new File(SystemConstant.USER_FILE_PATH + "\\" + folder.getPath() + folder.getName());
			
			folder.setPath(SystemConstant.toSQLString(folder.getPath()));
			//�ҳ����е���Ŀ¼
			List<Folder> targetFolder = folderMapper.selectAllFolderByFatherFolder(folder);
			//ɾ����Ŀ¼�µ��ļ�
			cloudFileMapper.deleteFileByFolderId(folder.getId());
			//ɾ����Ŀ¼�µ��ļ����ļ���
			for(Folder f : targetFolder)
			{
				cloudFileMapper.deleteFileByFolderId(f.getId());
				folderMapper.deleteFolderById(f.getId());
			}
			//���ɾ��Ŀ���ļ���
			folderMapper.deleteFolderById(folderId);
			
			//������ִ��ɾ��ʵ���ļ���
			if(target.exists())
			{
				SystemConstant.deleteDir(target);
				if(!target.exists())
				{
					sysMsg = "ɾ���ɹ�";
					sqlSession.commit();
				}
				else
				{
					sysMsg = "ɾ��ʧ�ܣ�������";
					sqlSession.rollback();
				}
			}
			else
			{
				sqlSession.rollback();
				sysMsg = "ɾ��ʧ��";
			}
		}
		catch(Exception e)
		{
			sysMsg = "ϵͳ�쳣";
			sqlSession.rollback();
			e.printStackTrace();
		}
		finally
		{
			model.addAttribute("sysMsg", sysMsg);
			sqlSession.close();
		}
		return "/main/file?dir=0";
	}
	
	@RequestMapping("/setAccess")
	public String accessFile(@RequestParam("folder")Integer folderId, @RequestParam("file")Integer fileId,
			Model model, HttpSession session)
	{
		User user = (User)session.getAttribute("user");
		if(user == null)
		{
			model.addAttribute("sysMsg","�����µ�¼");
			return "/index.jsp";
		}
		
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
		String sysMsg = "";
		try
		{
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);
			
			Folder folder;
			if(folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);
			
			CloudFile target = cloudFileMapper.selectFileById(fileId);
			
			Map<String, Object> params = new HashMap<>();
			if(target.getStatus() == CloudFile.STATUS_PRIVATE)
			{
				sysMsg = "�ɹ�������ļ�";
				params.put("access", CloudFile.STATUS_PUBLIC);
			}
			else
			{
				sysMsg = "��ȡ�����ļ�����";
				params.put("access", CloudFile.STATUS_PRIVATE);
			}
			params.put("fileId", target.getId());
			
			cloudFileMapper.changeFileAccessByFileId(params);
			sqlSession.commit();
		}
		catch(Exception e)
		{
			sysMsg = "ϵͳ�쳣";
			sqlSession.rollback();
			e.printStackTrace();
		}
		finally
		{
			model.addAttribute("sysMsg",sysMsg);
			sqlSession.close();
		}
		return "/main/file?dir=" + folderId;
	}
}
