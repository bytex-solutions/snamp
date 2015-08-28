namespace java com.bytex.snamp.testing.connectors.mda

struct MemoryStatus {
    1: required i32 free
    2: required i64 total
}

service MonitoringDataAcceptor{
    i16 get_short()
    i16 set_short(1:i16 value)

    i64 get_date()
    i64 set_date(1:i64 value)

    string get_biginteger()
    string set_biginteger(1:string value)

    string get_str()
    string set_str(1:string value)

    binary get_array()
    binary set_array(1:binary value)

    bool get_boolean()
    bool set_boolean(1:bool value)

    i64 get_long()
    i64 set_long(1:i64 value)

    MemoryStatus get_dict()
    MemoryStatus set_dict(1:i32 free, 2:i64 total)

    list<i64> get_longArray()
    list<i64> set_longArray(1:list<i64> value)

    oneway void notify_testEvent1(1:string message, 2:i64 seqnum, 3:i64 timeStamp, 4:i64 userData)
}