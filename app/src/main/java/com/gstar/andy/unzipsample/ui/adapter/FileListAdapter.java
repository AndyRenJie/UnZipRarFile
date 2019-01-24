package com.gstar.andy.unzipsample.ui.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.gstar.andy.unzipsample.R;
import com.gstar.andy.unzipsample.bean.FileModel;
import com.gstar.andy.unzipsample.utils.FileUtils;

import java.util.List;

/**
 * 文件列表适配器
 *
 * @author Andy.R
 */
public class FileListAdapter extends BaseQuickAdapter<FileModel, BaseViewHolder> {

    public FileListAdapter(int layoutResId, @Nullable List<FileModel> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, FileModel itemFileModel) {
        viewHolderHelper(helper, itemFileModel);
    }

    /**
     * viewHolder绑定数据
     *
     * @param helper
     * @param itemFileModel
     */
    private void viewHolderHelper(BaseViewHolder helper, FileModel itemFileModel) {
        if (itemFileModel != null) {
            helper.setText(R.id.tv_file_name, itemFileModel.getFileName());
            helper.setText(R.id.tv_file_date, itemFileModel.getFileDateShow());
            helper.setText(R.id.tv_file_size, itemFileModel.getFileSizeShow());
            helper.setImageResource(R.id.iv_file_icon, FileUtils.getFileIcon(
                    itemFileModel.isDir(), itemFileModel.getFilePath()));
            if (itemFileModel.isDir()) {
                helper.setVisible(R.id.tv_file_size, false);
            } else {
                helper.setVisible(R.id.tv_file_size, true);
            }
        }
    }
}
