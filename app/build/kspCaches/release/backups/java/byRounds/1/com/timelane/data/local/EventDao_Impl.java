package com.timelane.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EventDao_Impl implements EventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EventEntity> __insertionAdapterOfEventEntity;

  private final EntityDeletionOrUpdateAdapter<EventEntity> __updateAdapterOfEventEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteEvent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllEvents;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldEvents;

  public EventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEventEntity = new EntityInsertionAdapter<EventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `events` (`id`,`title`,`startTime`,`durationMinutes`,`linkedTaskId`,`isCompleted`,`soundRes`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EventEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getStartTime());
        statement.bindLong(4, entity.getDurationMinutes());
        if (entity.getLinkedTaskId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getLinkedTaskId());
        }
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getSoundRes() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getSoundRes());
        }
      }
    };
    this.__updateAdapterOfEventEntity = new EntityDeletionOrUpdateAdapter<EventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `events` SET `id` = ?,`title` = ?,`startTime` = ?,`durationMinutes` = ?,`linkedTaskId` = ?,`isCompleted` = ?,`soundRes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EventEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getStartTime());
        statement.bindLong(4, entity.getDurationMinutes());
        if (entity.getLinkedTaskId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getLinkedTaskId());
        }
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getSoundRes() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getSoundRes());
        }
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteEvent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM events WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllEvents = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM events";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldEvents = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM events WHERE startTime < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertEvent(final EventEntity event, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfEventEntity.insertAndReturnId(event);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateEvent(final EventEntity event, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEventEntity.handle(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEvent(final long eventId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteEvent.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, eventId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteEvent.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllEvents(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllEvents.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllEvents.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldEvents(final String isoTimestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldEvents.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, isoTimestamp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldEvents.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EventEntity>> getAllEvents() {
    final String _sql = "SELECT * FROM events ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"events"}, new Callable<List<EventEntity>>() {
      @Override
      @NonNull
      public List<EventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfLinkedTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedTaskId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfSoundRes = CursorUtil.getColumnIndexOrThrow(_cursor, "soundRes");
          final List<EventEntity> _result = new ArrayList<EventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final Long _tmpLinkedTaskId;
            if (_cursor.isNull(_cursorIndexOfLinkedTaskId)) {
              _tmpLinkedTaskId = null;
            } else {
              _tmpLinkedTaskId = _cursor.getLong(_cursorIndexOfLinkedTaskId);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final Integer _tmpSoundRes;
            if (_cursor.isNull(_cursorIndexOfSoundRes)) {
              _tmpSoundRes = null;
            } else {
              _tmpSoundRes = _cursor.getInt(_cursorIndexOfSoundRes);
            }
            _item = new EventEntity(_tmpId,_tmpTitle,_tmpStartTime,_tmpDurationMinutes,_tmpLinkedTaskId,_tmpIsCompleted,_tmpSoundRes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<EventEntity>> getEventsForDay(final String datePattern) {
    final String _sql = "SELECT * FROM events WHERE startTime LIKE ? || '%' ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, datePattern);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"events"}, new Callable<List<EventEntity>>() {
      @Override
      @NonNull
      public List<EventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfLinkedTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedTaskId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfSoundRes = CursorUtil.getColumnIndexOrThrow(_cursor, "soundRes");
          final List<EventEntity> _result = new ArrayList<EventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final Long _tmpLinkedTaskId;
            if (_cursor.isNull(_cursorIndexOfLinkedTaskId)) {
              _tmpLinkedTaskId = null;
            } else {
              _tmpLinkedTaskId = _cursor.getLong(_cursorIndexOfLinkedTaskId);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final Integer _tmpSoundRes;
            if (_cursor.isNull(_cursorIndexOfSoundRes)) {
              _tmpSoundRes = null;
            } else {
              _tmpSoundRes = _cursor.getInt(_cursorIndexOfSoundRes);
            }
            _item = new EventEntity(_tmpId,_tmpTitle,_tmpStartTime,_tmpDurationMinutes,_tmpLinkedTaskId,_tmpIsCompleted,_tmpSoundRes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getEventById(final long id, final Continuation<? super EventEntity> $completion) {
    final String _sql = "SELECT * FROM events WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EventEntity>() {
      @Override
      @Nullable
      public EventEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfLinkedTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedTaskId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfSoundRes = CursorUtil.getColumnIndexOrThrow(_cursor, "soundRes");
          final EventEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final Long _tmpLinkedTaskId;
            if (_cursor.isNull(_cursorIndexOfLinkedTaskId)) {
              _tmpLinkedTaskId = null;
            } else {
              _tmpLinkedTaskId = _cursor.getLong(_cursorIndexOfLinkedTaskId);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final Integer _tmpSoundRes;
            if (_cursor.isNull(_cursorIndexOfSoundRes)) {
              _tmpSoundRes = null;
            } else {
              _tmpSoundRes = _cursor.getInt(_cursorIndexOfSoundRes);
            }
            _result = new EventEntity(_tmpId,_tmpTitle,_tmpStartTime,_tmpDurationMinutes,_tmpLinkedTaskId,_tmpIsCompleted,_tmpSoundRes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getFutureEvents(final String isoTimestamp, final long excludeId,
      final Continuation<? super List<EventEntity>> $completion) {
    final String _sql = "SELECT * FROM events WHERE startTime >= ? AND id != ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, isoTimestamp);
    _argIndex = 2;
    _statement.bindLong(_argIndex, excludeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EventEntity>>() {
      @Override
      @NonNull
      public List<EventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfLinkedTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedTaskId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfSoundRes = CursorUtil.getColumnIndexOrThrow(_cursor, "soundRes");
          final List<EventEntity> _result = new ArrayList<EventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final Long _tmpLinkedTaskId;
            if (_cursor.isNull(_cursorIndexOfLinkedTaskId)) {
              _tmpLinkedTaskId = null;
            } else {
              _tmpLinkedTaskId = _cursor.getLong(_cursorIndexOfLinkedTaskId);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final Integer _tmpSoundRes;
            if (_cursor.isNull(_cursorIndexOfSoundRes)) {
              _tmpSoundRes = null;
            } else {
              _tmpSoundRes = _cursor.getInt(_cursorIndexOfSoundRes);
            }
            _item = new EventEntity(_tmpId,_tmpTitle,_tmpStartTime,_tmpDurationMinutes,_tmpLinkedTaskId,_tmpIsCompleted,_tmpSoundRes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllEventsList(final Continuation<? super List<EventEntity>> $completion) {
    final String _sql = "SELECT * FROM events";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EventEntity>>() {
      @Override
      @NonNull
      public List<EventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfLinkedTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedTaskId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfSoundRes = CursorUtil.getColumnIndexOrThrow(_cursor, "soundRes");
          final List<EventEntity> _result = new ArrayList<EventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final Long _tmpLinkedTaskId;
            if (_cursor.isNull(_cursorIndexOfLinkedTaskId)) {
              _tmpLinkedTaskId = null;
            } else {
              _tmpLinkedTaskId = _cursor.getLong(_cursorIndexOfLinkedTaskId);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final Integer _tmpSoundRes;
            if (_cursor.isNull(_cursorIndexOfSoundRes)) {
              _tmpSoundRes = null;
            } else {
              _tmpSoundRes = _cursor.getInt(_cursorIndexOfSoundRes);
            }
            _item = new EventEntity(_tmpId,_tmpTitle,_tmpStartTime,_tmpDurationMinutes,_tmpLinkedTaskId,_tmpIsCompleted,_tmpSoundRes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
