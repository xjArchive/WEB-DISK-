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
			model.addAttribute("请选择文件");
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
				int size = (int)(files.getSize() / 1024);  //文件大小，默认显示大小1kb
				if(size == 0)
					size = 1;
				
				int sub = files.getOriginalFilename().lastIndexOf('.');

				Map<String,Object> params = new HashMap<>();  //封装文件信息
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
			sysMsg = "文件上传成功";
			
		}
		catch(Exception e)
		{
			sysMsg = "请选择文件";
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
			model.addAttribute("sysMsg", "请输入文件夹名");
			return "/main/file?dir=0";
		}
		if(!SystemConstant.isValidFileName(folderName))
		{
			model.addAttribute("sysMsg", "文件夹名不规范，请重新输入");
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
			sysMsg = "上传成功";
		}
		catch(Exception e)
		{
			sysMsg = "系统异常";
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
			sysMsg = "删除成功";
		}
		catch(Exception e)
		{
			sqlSession.rollback();
			sysMsg = "系统异常";
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
				model.addAttribute("sysMsg", "你没有权限删除该文件夹");
				sqlSession.close();
				return "/index.jsp";
			}
			File target = new File(SystemConstant.USER_FILE_PATH + "\\" + folder.getPath() + folder.getName());
			
			folder.setPath(SystemConstant.toSQLString(folder.getPath()));
			//找出所有的子目录
			List<Folder> targetFolder = folderMapper.selectAllFolderByFatherFolder(folder);
			//删除根目录下的文件
			cloudFileMapper.deleteFileByFolderId(folder.getId());
			//删除子目录下的文件和文件夹
			for(Folder f : targetFolder)
			{
				cloudFileMapper.deleteFileByFolderId(f.getId());
				folderMapper.deleteFolderById(f.getId());
			}
			//最后删除目标文件夹
			folderMapper.deleteFolderById(folderId);
			
			//服务器执行删除实际文件夹
			if(target.exists())
			{
				SystemConstant.deleteDir(target);
				if(!target.exists())
				{
					sysMsg = "删除成功";
					sqlSession.commit();
				}
				else
				{
					sysMsg = "删除失败，请重试";
					sqlSession.rollback();
				}
			}
			else
			{
				sqlSession.rollback();
				sysMsg = "删除失败";
			}
		}
		catch(Exception e)
		{
			sysMsg = "系统异常";
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
			model.addAttribute("sysMsg","请重新登录");
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
				sysMsg = "成功分享该文件";
				params.put("access", CloudFile.STATUS_PUBLIC);
			}
			else
			{
				sysMsg = "已取消该文件分享";
				params.put("access", CloudFile.STATUS_PRIVATE);
			}
			params.put("fileId", target.getId());
			
			cloudFileMapper.changeFileAccessByFileId(params);
			sqlSession.commit();
		}
		catch(Exception e)
		{
			sysMsg = "系统异常";
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
