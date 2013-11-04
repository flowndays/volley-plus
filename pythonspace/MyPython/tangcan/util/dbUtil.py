__author__ = 'TC'

import sqlite3

def createTable(dbfile):
    conn = sqlite3.connect(dbfile)
    cur = conn.cursor()
    cur.execute(
        'CREATE TABLE  if not exists book (_Id INTEGER PRIMARY KEY, name text, author text, status int, sequence int, brief text, lReadPlace real, fileName text, size int)')
    conn.commit()
    cur.execute(
        'CREATE TABLE  if not exists chapter(_Id INTEGER PRIMARY KEY, title text, bookId int, sequence int, start int, end int)')
    conn.commit()