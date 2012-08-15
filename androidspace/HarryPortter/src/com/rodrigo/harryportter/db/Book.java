package com.rodrigo.harryportter.db;

public class Book implements Comparable<Book> {
	/*
	 * book表的字段： _Id INTEGER, name text, author text, status tinyint, sequence
	 * long brief text, lReadPlace int, lReadWords text
	 */
	private long _Id = -1;
	private String fileName;
	private String name;
	private String author;
	private short status = -1;// 暂时不用
	private int sequence = -1;// 序号
	private String brief;// 简介
	private int lReadPlace = 0;// 上次阅读位置
	private int size = -1;// 总长度

	public long get_Id() {
		return _Id;
	}

	public void set_Id(long _Id) {
		this._Id = _Id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	public int getlReadPlace() {
		return lReadPlace;
	}

	public void setlReadPlace(int lReadPlace) {
		this.lReadPlace = lReadPlace;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Book))
			return false;

		return this._Id == ((Book) o).get_Id();
	}

	@Override
	public int compareTo(Book another) {
		return (int) (this.sequence - another.sequence);
	}
}
