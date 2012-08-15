package com.rodrigo.harryportter.ui.activity;

import java.io.*;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.rodrigo.harryportter.R;
import com.rodrigo.harryportter.db.Book;
import com.rodrigo.harryportter.db.DBInstance;
import com.rodrigo.harryportter.uitil.io.IOUtils;
import com.rodrigo.harryportter.util.Constants;
import com.rodrigo.harryportter.util.FileCleanUtils;
import com.rodrigo.harryportter.util.ZipUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final int NO_SD = 1;
    private static final int LOAD_SUCCESS = 2;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBInstance.getInstance(this).registerDb();

        File dir = new File(Constants.SD_BOOK_DIR + "books/");
        if (!dir.exists() || DBInstance.getInstance(MainActivity.this).selectBookCount() == 0) {
            setContentView(R.layout.main);
            Toast.makeText(this, "文件只需准备一次，以后就能快速打开了！", Toast.LENGTH_LONG).show();
            new DeleteTask().execute(new Void[0]);
        } else {
            getIn();
        }

    }

    private void getIn() {
        Intent intent = new Intent(MainActivity.this, BookListActivity.class);
        startActivity(intent);
        finish();
    }


    private class DeleteTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)
                    || Environment.getExternalStorageState().equals(Environment.MEDIA_SHARED)) {
                return NO_SD;
            } else {
                prepareBooks();
            }
            return LOAD_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {
            DBInstance.getInstance(MainActivity.this).close();
            switch (result) {
                case NO_SD:
                    new AlertDialog.Builder(MainActivity.this).setTitle("找不到SD卡")
                            .setMessage("小说下载阅读器需要SD卡才能运行，请确认手机SD卡是否可用!")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    MainActivity.this.finish();
                                    System.exit(0);
                                }
                            }).show();
                    break;
                case LOAD_SUCCESS:
                    getIn();
                default:
                    break;
            }
            super.onPostExecute(result);
        }

    }

    private void prepareBooks() {
        File dir = new File(Constants.SD_BOOK_DIR + "books/");
        if (!dir.exists() || DBInstance.getInstance(MainActivity.this).selectBookCount() == 0)
            if (getFiles()) {
                DBInstance.getInstance(this).deleteAllBook();
                String[] files = dir.list();
                ArrayList<Book> books = getBookList();
                if (books != null && !books.isEmpty())
                    for (Book book : books) {
                        DBInstance.getInstance(this).saveBook(book);
                    }
            }

    }

    public ArrayList<Book> getBookList() {
        InputStream inputStream = getResources().openRawResource(R.raw.books);
        ArrayList<Book> result = new ArrayList<Book>();
        try {
            String listStr = IOUtils.toString(inputStream);
            JSONArray jsonArray = new JSONArray(listStr);
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                JSONObject object = jsonArray.getJSONObject(i);
                Book book = new Book();
                book.setName(object.getString("name"));
                book.setFileName(object.getString("file"));
                book.setSequence(i);
                result.add(book);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            result.clear();
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            result.clear();
        }
        return result;
    }

    private boolean getFiles() {
        File existed = new File(Constants.SD_BOOK_DIR + "books/");
        if (existed.exists()) {
            FileCleanUtils.deleteAll(existed);
        }
        File zipFile = new File(Constants.SD_BOOK_DIR + "books.zip");
        if (zipFile.exists())
            zipFile.delete();
        final AssetManager assetManager = getAssets();
        final InputStream in;
        final OutputStream out;
        try {
            final File dir = new File(Constants.SD_BOOK_DIR);
            if (!dir.exists())
                dir.mkdirs();
            in = assetManager.open("books.zip");
            out = new FileOutputStream(zipFile);
            IOUtils.copy(in, out);
            in.close();
            out.close();
            ZipUtil.unZip(Constants.SD_BOOK_DIR + "books.zip", Constants.SD_BOOK_DIR);
            return true;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
            return false;
        }
    }
}