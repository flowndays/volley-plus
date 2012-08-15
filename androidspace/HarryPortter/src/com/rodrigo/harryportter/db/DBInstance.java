package com.rodrigo.harryportter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rodrigo.harryportter.util.StringUtil;

/**
 * 注意：select *
 * 的查询语句不能随意修改为查询部分字段，如果要修改，需要修改对应方法中的cur.getInt()参数改成修改后的columeIndex
 * 
 * @author TC
 * 
 */
public class DBInstance {
	private static DBInstance instance = null;
	private SQLiteDatabase db = null;
	private DBOpenHelper dbHelper = null;

	private int reference;

	private static final String SELECT_BOOK_BY_ID = "SELECT * FROM book WHERE _id = ?";
	private static final String SELECT_ALL_BOOKS = "SELECT * FROM book";
	private static final String SELECT_MAX_BOOK_SEQ = "SELECT sequence FROM book order by sequence desc limit 0,1";
	private static final String SELECT_BOOK_WITH_ORDER = "select * from book order by sequence";

	private DBInstance(Context ctx) {
		reference = 0;
		// dbHelper = new DBOpenHelper(new ContextWrapper(ctx),
		// DBOpenHelper.DB_NAME, null, DBOpenHelper.DB_VERSION);
		dbHelper = new DBOpenHelper(ctx, DBOpenHelper.DB_NAME, null, DBOpenHelper.DB_VERSION);
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 获取数据库实例。 每个线程在启动时，需要注册，调用：DBInstance.getInstance(this).registerDb();
	 * 在destroy时，需要关闭DB，调用：DBInstance.getInstance(this).close();
	 * 
	 * @param ctx
	 * @return
	 */
	public static DBInstance getInstance(Context ctx) {
		if (instance != null)
			return instance;
		synchronized (DBInstance.class) {
			if (instance == null) {
				instance = new DBInstance(ctx);
			}
		}
		return instance;
	}

	public void registerDb() {
		reference++;
	}

	/**
	 * 注销数据库用户，如果没有线程在使用此db了，则关闭数据库。
	 * 所有使用此数据库的线程，都需要在启动时注册（registerDb），并在销毁时关闭（close）
	 */
	public void close() {
		reference--;
		if (reference == 0) {
			synchronized (this) {
				instance = null;
				db.close();
				dbHelper.close();
				db = null;
				dbHelper = null;
			}
		}
	}

	public Book selectBookByID(long id) {
		Cursor cur = db.rawQuery(SELECT_BOOK_BY_ID, new String[] { id + "" });
		if (cur == null) {
			return null;
		}
		/*
		 * book表的字段： _Id INTEGER, name text, author text, status tinyint,
		 * sequence long brief text, lReadPlace int, lReadWords text
		 */
		if (cur.moveToFirst()) {
			Book book = new Book();
			book.set_Id(cur.getInt(0));
			book.setName(cur.getString(1));
			book.setAuthor(cur.getString(2));
			book.setStatus(cur.getShort(3));
			book.setSequence(cur.getInt(4));
			book.setBrief(cur.getString(5));
			book.setlReadPlace(cur.getInt(6));
			book.setFileName(cur.getString(7));
			book.setSize(cur.getInt(8));
			cur.close();
			return book;
		} else {
			cur.close();
			return null;
		}
	}

	public Book[] selectAllBooks() {
		Cursor cur = db.rawQuery(SELECT_BOOK_WITH_ORDER, null);
		// Cursor cur = db.rawQuery(SELECT_ALL_BOOKS, null);
		if (cur == null) {
			return null;
		}

		if (cur.moveToFirst()) {
			Book[] books = new Book[cur.getCount()];
			do {
				Book book = new Book();
				book.set_Id(cur.getInt(0));
				book.setName(cur.getString(1));
				book.setAuthor(cur.getString(2));
				book.setStatus(cur.getShort(3));
				book.setSequence(cur.getInt(4));
				book.setBrief(cur.getString(5));
				book.setlReadPlace(cur.getInt(6));
				book.setFileName(cur.getString(7));
				book.setSize(cur.getInt(8));
				books[cur.getPosition()] = book;
			} while (cur.moveToNext());
			cur.close();
			return books;
		} else {
			cur.close();
			return null;
		}
	}

	public int selectBookCount() {
		Cursor cur = db.rawQuery("select count(*) from book", new String[] {});
		if (cur == null) {
			return 0;
		}
		if (cur.moveToFirst()) {
			return cur.getInt(0);
		} else {
			cur.close();
			return 0;
		}
	}

	public int saveBook(Book book) {
		db.execSQL("delete from book where fileName = '" + book.getFileName() + "'");
		ContentValues cv = new ContentValues();
		/*
		 * book表的字段： _Id INTEGER, name text, author text, status tinyint,
		 * sequence long brief text, lReadPlace int, lReadWords text
		 */
		cv.put("fileName", StringUtil.validate(book.getFileName()));
		cv.put("name", StringUtil.validate(book.getName()));
		cv.put("author", StringUtil.validate(book.getAuthor()));
		cv.put("status", book.getStatus());
		cv.put("sequence", book.getSequence());
		cv.put("brief", StringUtil.validate(book.getBrief()));
		cv.put("lReadPlace", book.getlReadPlace());
		cv.put("size", book.getSize());

		return (int) db.insert(DBOpenHelper.DB_TABLENAME_BOOK, null, cv);
	}

	public int getMaxBookSequence() {
		Cursor cur = db.rawQuery(SELECT_MAX_BOOK_SEQ, null);
		if (cur == null)
			return -1;

		if (!cur.moveToFirst()) {
			cur.close();
			return -1;
		}

		int sequece = cur.getInt(cur.getColumnIndex("sequence"));
		cur.close();
		return sequece;
	}

	/**
	 * 删除书籍时同时删除相关的章节
	 * 
	 * @param bookId
	 * @return
	 */
	public boolean deleteBook(long bookId) {
		long result = db.delete(DBOpenHelper.DB_TABLENAME_BOOK, "_Id=" + bookId, null);
		return result == 1;
	}

    public int deleteAllBook() {
        return db.delete(DBOpenHelper.DB_TABLENAME_BOOK, "1=1", null);
    }

	public int updateBook(Book book) {
		if (book.get_Id() == -1)
			return 0;
		ContentValues cv = new ContentValues();
		if (book.getName() != null)
			cv.put("name", book.getName());
		if (book.getFileName() != null)
			cv.put("fileName", book.getFileName());
		if (book.getAuthor() != null)
			cv.put("author", book.getAuthor());
		if (book.getBrief() != null)
			cv.put("brief", book.getBrief());
		if (book.getSequence() != -1)
			cv.put("sequence", book.getSequence());
		if (book.getStatus() != -1)
			cv.put("status", book.getStatus());
		if (book.getlReadPlace() != -1)
			cv.put("lReadPlace", book.getlReadPlace());
		if (book.getSize() != -1)
			cv.put("size", book.getSize());
		return db.update(DBOpenHelper.DB_TABLENAME_BOOK, cv, "_Id=" + book.get_Id(), null);
	}

	public int updateBooks(Book[] books) {
		if (books == null || books.length == 0)
			return 0;
		int updateCount = 0;
		db.beginTransaction();
		for (Book book : books) {
			updateCount += updateBook(book);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		return updateCount;
	}
}
