package com.example.ex1.AsynTask;

import static com.example.ex1.MainActivity.fileAdapter;

import android.os.AsyncTask;
import com.example.ex1.Model.File;

public class DownloadTask extends AsyncTask<File, File, File> {
//                                        input, in progress , output
    @Override
    protected File doInBackground(File... files) {
        File file = files[0];
        double downloadCapacity = 0;
        while (downloadCapacity < file.getCapacity()){
            try {
                Thread.sleep(1000);
                downloadCapacity += 0.1;

                int currentProgress = (int) (100 * downloadCapacity / file.getCapacity());
                file.setProgress(currentProgress);

                publishProgress(file);
            }catch (InterruptedException e){
                file.setStatus(2);
                return file;
            }
        }
        file.setStatus(3);
        return file;
    }

    @Override
    protected void onProgressUpdate(File... values) {
        fileAdapter.notifyDataSetChanged();
    }
}
