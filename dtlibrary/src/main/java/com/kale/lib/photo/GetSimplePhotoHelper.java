package com.kale.lib.photo;


import com.kale.lib.utils.BitmapUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * @author Jack Tony
 *         从相册或者从照相机得到一个图片，没有裁剪功能
 * @date 2015/4/24
 */
public class GetSimplePhotoHelper {

    public static final int FROM_ALBUM = 0;

    public static final int FROM_CAMERA = 1;

    @IntDef({FROM_ALBUM, FROM_CAMERA})
    private @interface from {

    }

    private Activity mActivity;

    private String mPicFilePath;

    private int mFromWay;

    private GetSimplePhotoHelper(Activity activity) {
        mActivity = activity;
    }

    private static GetSimplePhotoHelper instance;

    public static GetSimplePhotoHelper getInstance(Activity activity) {
        if (instance == null) {
            instance = new GetSimplePhotoHelper(activity);
        }
        return instance;
    }

    /**
     * 从相册或照相机获得一张图片
     *
     * @param way         获取图片的途径
     * @param picFilePath 如果需要保存从相机拍摄的图片，请指定保存图片的全部路径(通过相机拍照时才有效)
     *                    eg:GetPhotoHelper.choicePhoto(GetPhotoHelper.FROM_WAY.FROM_CAMERA, Environment.getExternalStorageDirectory()+ "/temp.jpg");
     * @param listener    得到图片后触发的监听器
     */
    public void choicePhoto(@from final int way, @Nullable String picFilePath, OnSelectedPhotoListener listener) {
        mFromWay = way;
        mPicFilePath = picFilePath;
        if (way == FROM_ALBUM) {
            choicePhotoFromAlbum();
        } else if (way == FROM_CAMERA) {
            choicePhotoFromCamera(picFilePath);
        }
        mListener = listener;
    }

    /**
     * 启动相册的activity
     */
    private void choicePhotoFromAlbum() {
        Intent intent = new Intent(mActivity, GetSimplePhotoActivity.class);
        intent.putExtra(GetSimplePhotoActivity.KEY_FROM_WAY, GetSimplePhotoActivity.VALUE_FROM_ALBUM);
        mActivity.startActivityForResult(intent, 0);
    }

    /**
     * 启动相机的activity
     */
    private void choicePhotoFromCamera(String picFilePath) {
        Intent intent = new Intent(mActivity, GetSimplePhotoActivity.class);
        intent.putExtra(GetSimplePhotoActivity.KEY_FROM_WAY, GetSimplePhotoActivity.VALUE_FROM_CAMERA);
        intent.putExtra(GetSimplePhotoActivity.KEY_PHOTO_PATH, picFilePath);
        mActivity.startActivityForResult(intent, 0);
    }


    /**
     * 得到已经选择好的图片，这个方法必须在onActivityResult中进行回调
     *
     * @return 已经选择好的bitmap
     */
    protected void getSelectedPhoto(Uri uri) {
        //Logger.d("uri = " + uri);
        Bitmap bitmap = BitmapFactory.decodeFile(uri.toString());
        // Logger.d("方向 =" + GetSimplePhotoUtil.getOrientation(uri));
        if (bitmap != null) {
            bitmap = BitmapUtil.rotateBitmap(bitmap, GetSimplePhotoUtil.getPhotoDegreeByUri(uri));
        }
        SimplePhoto photo = new SimplePhoto();
        photo.bitmap = bitmap;
        photo.uri = uri;
        photo.degree = GetSimplePhotoUtil.getPhotoDegreeByUri(uri);

        // 如果来源是相机，而且没有指定图片保存的目录，那么使用完毕后就立刻删除相片
        if (mFromWay == FROM_CAMERA && mPicFilePath == null) {
            File tempPicFile = new File(uri.toString());
            if (tempPicFile != null) {
                tempPicFile.delete();//设置成功后清除之前的照片文件
            }
        }
        mListener.onSelectedPhoto(mFromWay,photo);
    }

    private OnSelectedPhotoListener mListener;

    public interface OnSelectedPhotoListener {

        public void onSelectedPhoto(int way,SimplePhoto photo);

    }

}
