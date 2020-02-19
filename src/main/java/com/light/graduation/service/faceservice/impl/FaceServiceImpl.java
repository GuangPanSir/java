package com.light.graduation.service.faceservice.impl;

import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.toolkit.ImageInfo;
import com.light.graduation.dao.StudentDao;
import com.light.graduation.entity.Student;
import com.light.graduation.service.faceservice.FaceService;
import com.light.graduation.utils.GetFaceEngine;
import com.light.graduation.utils.ImageConvertUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.arcsoft.face.toolkit.ImageFactory.getRGBData;

/**
 * @Author: Light
 * @Date 2020/1/14 11:04
 */
@Data
@NoArgsConstructor
@Service
public class FaceServiceImpl implements FaceService {
	@Autowired
	private StudentDao studentDao;
	
	/**
	 * 获取人脸识别引擎
	 */
	private FaceEngine faceEngine = GetFaceEngine.getFaceEngine ( );
	
	/**
	 * 图片编码
	 */
	private String imgStr;
	
	/**
	 * 图片信息
	 */
	public ImageInfo imageInfo = new ImageInfo ();
	
	/**
	 * 获取人脸信息
	 */
	public List< FaceInfo > faceInfoList = new ArrayList<>();
	
	/**
	 * 人脸信息
	 */
	public FaceFeature faceFeature = new FaceFeature (  );
	
	public FaceServiceImpl ( String imgStr ) {
		this.imgStr = imgStr;
	}
	
	@Override
	public boolean detectFaces (   ) {
		//首先对标志位赋初值，图片中识别到人脸
		boolean flag = true;
		if ( getFaceInfo (  ).size ( ) == 0 ) {
			flag = false;
		}
		return flag;
	}
	
	@Override
	public int faceFeatureLength (   ) {
		return faceFeature (   ).length;
	}
	
	@Override
	public byte[] faceFeature (  ) {
		faceInfoList = getFaceInfo (  );
		faceEngine.extractFaceFeature ( imageInfo.getImageData ( ) , imageInfo.getWidth ( ) , imageInfo.getHeight ( ) , imageInfo.getImageFormat ( ) , faceInfoList.get ( 0 ) , faceFeature );
		return faceFeature.getFeatureData ( );
	}
	
	@Override
	public FaceFeature getFaceFeature (   ) {
		faceFeature (  );
		return faceFeature;
	}
	
	@Override
	public List< FaceInfo > getFaceInfo (   ) {
		//首先对标志位赋初值，图片中识别到人脸
		byte[] imageByte = ImageConvertUtils.base64String2ByteFun ( imgStr );
		imageInfo = getRGBData ( imageByte );
		if ( imageInfo == null ) {
			System.out.println ( "图片错误" );
		}
		faceEngine.detectFaces ( imageInfo.getImageData ( ) , imageInfo.getWidth ( ) , imageInfo.getHeight ( ) , imageInfo.getImageFormat ( ) , faceInfoList );
		System.out.println ( imageInfo );
		return faceInfoList;
	}
	
	@Override
	public float faceSimilarScore ( @NotNull FaceFeature targetFaceFeature , FaceSimilar faceSimilar ) {
		targetFaceFeature.setFeatureData ( targetFaceFeature.getFeatureData ( ) );
		this.faceFeature.setFeatureData ( this.faceFeature.getFeatureData ( ) );
		faceEngine.compareFaceFeature ( targetFaceFeature , this.faceFeature , faceSimilar );
		return faceSimilar.getScore ( );
	}
	
	@Override
	public boolean faceCompare ( FaceFeature targetFaceFeature , FaceSimilar faceSimilar ) {
		boolean flag = false;
		float checkSimilarScore = 0.8f;
		if ( faceSimilarScore ( targetFaceFeature,  faceSimilar ) > checkSimilarScore ) {
			flag = true;
		}
		return flag;
	}
	
	@Override
	public boolean faceCompare ( String targetImgStr , String sourceImgStr ) {
		FaceServiceImpl faceServiceImpl01 = new FaceServiceImpl (targetImgStr );
		faceServiceImpl01.getFaceFeature (   );
		FaceServiceImpl faceServiceImpl02 = new FaceServiceImpl ( sourceImgStr );
		FaceFeature faceFeature02 = faceServiceImpl02.getFaceFeature (   );
		return faceServiceImpl01.faceCompare ( faceFeature02 , new FaceSimilar ( ) );
	}
	
	public void updateStudentImage ( Student student ) {
		this.studentDao.updateByPrimaryKeySelective ( student );
	}
	
	@Override
	public List<Student> selectAll ( ) {
		return this.studentDao.selectAllStudents ( );
	}
	
}