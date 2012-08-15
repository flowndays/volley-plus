package com.rodrigo.harryportter.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rodrigo.harryportter.R;
import com.rodrigo.harryportter.db.Book;
import com.rodrigo.harryportter.db.DBInstance;
import com.rodrigo.harryportter.util.Constants;

public class BookListActivity extends Activity {
	ListView mListView;
	BaseAdapter mAdapter;
	Book[] mBooks;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_list);
		DBInstance.getInstance(this).registerDb();

		mListView = (ListView) findViewById(R.id.lBookList);

		mBooks = DBInstance.getInstance(this).selectAllBooks();

		mAdapter = new BookAdapter(this);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(BookListActivity.this, ReadActivity.class);
				intent.putExtra("bookId", id);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		mBooks = DBInstance.getInstance(this).selectAllBooks();
		mAdapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		DBInstance.getInstance(this).close();
		super.onDestroy();
	}

	class BookAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		Context ctx;
		Bitmap defaultCover;

		public BookAdapter(Context ctx) {
			super();
			this.ctx = ctx;
			mInflater = LayoutInflater.from(ctx);
			defaultCover = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.book_cover_default);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup vg) {
			ViewHolder holder;
			Book book = mBooks[position];
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.book_list_item, null);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.listBookName);
				holder.bookCover = (ImageView) convertView.findViewById(R.id.listCover);
				holder.lastRead = (TextView) convertView.findViewById(R.id.listLastRead);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.name.setText("");
				holder.bookCover.setImageBitmap(null);
				holder.lastRead.setText("");
			}

			holder.name.setText(" " + book.getName());
			Bitmap cover = null;
			if (position > Constants.covers.length - 1) {
				cover = defaultCover;
			} else {
				cover = BitmapFactory.decodeResource(ctx.getResources(), Constants.covers[position]);
			}
			holder.bookCover.setImageBitmap(cover);
			if (book.getlReadPlace() == -1)
				holder.lastRead.setText("未阅读");
			else
				holder.lastRead.setText("阅读进度: " + book.getlReadPlace() * 100 / book.getSize() + "%");

			convertView.setId((int) book.get_Id());
			return convertView;
		}

		public class ViewHolder {
			long bookId;
			ImageView bookCover;
			TextView name;
			TextView lastRead;
		}

		@Override
		public long getItemId(int p) {
			return mBooks[p].get_Id();
		}

		@Override
		public Object getItem(int p) {
			return mBooks[p];
		}

		@Override
		public int getCount() {
			return mBooks.length;
		}
	}
}
