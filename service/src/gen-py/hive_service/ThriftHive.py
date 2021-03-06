#
# Autogenerated by Thrift
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#

from thrift.Thrift import *
import hive_metastore.ThriftHiveMetastore
from ttypes import *
from thrift.Thrift import TProcessor
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
try:
  from thrift.protocol import fastbinary
except:
  fastbinary = None


class Iface(hive_metastore.ThriftHiveMetastore.Iface):
  def execute(self, query):
    pass

  def fetchOne(self, ):
    pass

  def fetchN(self, numRows):
    pass

  def fetchAll(self, ):
    pass

  def getSchema(self, ):
    pass


class Client(hive_metastore.ThriftHiveMetastore.Client, Iface):
  def __init__(self, iprot, oprot=None):
    hive_metastore.ThriftHiveMetastore.Client.__init__(self, iprot, oprot)

  def execute(self, query):
    self.send_execute(query)
    self.recv_execute()

  def send_execute(self, query):
    self._oprot.writeMessageBegin('execute', TMessageType.CALL, self._seqid)
    args = execute_args()
    args.query = query
    args.write(self._oprot)
    self._oprot.writeMessageEnd()
    self._oprot.trans.flush()

  def recv_execute(self, ):
    (fname, mtype, rseqid) = self._iprot.readMessageBegin()
    if mtype == TMessageType.EXCEPTION:
      x = TApplicationException()
      x.read(self._iprot)
      self._iprot.readMessageEnd()
      raise x
    result = execute_result()
    result.read(self._iprot)
    self._iprot.readMessageEnd()
    if result.ex != None:
      raise result.ex
    return

  def fetchOne(self, ):
    self.send_fetchOne()
    return self.recv_fetchOne()

  def send_fetchOne(self, ):
    self._oprot.writeMessageBegin('fetchOne', TMessageType.CALL, self._seqid)
    args = fetchOne_args()
    args.write(self._oprot)
    self._oprot.writeMessageEnd()
    self._oprot.trans.flush()

  def recv_fetchOne(self, ):
    (fname, mtype, rseqid) = self._iprot.readMessageBegin()
    if mtype == TMessageType.EXCEPTION:
      x = TApplicationException()
      x.read(self._iprot)
      self._iprot.readMessageEnd()
      raise x
    result = fetchOne_result()
    result.read(self._iprot)
    self._iprot.readMessageEnd()
    if result.success != None:
      return result.success
    if result.ex != None:
      raise result.ex
    raise TApplicationException(TApplicationException.MISSING_RESULT, "fetchOne failed: unknown result");

  def fetchN(self, numRows):
    self.send_fetchN(numRows)
    return self.recv_fetchN()

  def send_fetchN(self, numRows):
    self._oprot.writeMessageBegin('fetchN', TMessageType.CALL, self._seqid)
    args = fetchN_args()
    args.numRows = numRows
    args.write(self._oprot)
    self._oprot.writeMessageEnd()
    self._oprot.trans.flush()

  def recv_fetchN(self, ):
    (fname, mtype, rseqid) = self._iprot.readMessageBegin()
    if mtype == TMessageType.EXCEPTION:
      x = TApplicationException()
      x.read(self._iprot)
      self._iprot.readMessageEnd()
      raise x
    result = fetchN_result()
    result.read(self._iprot)
    self._iprot.readMessageEnd()
    if result.success != None:
      return result.success
    if result.ex != None:
      raise result.ex
    raise TApplicationException(TApplicationException.MISSING_RESULT, "fetchN failed: unknown result");

  def fetchAll(self, ):
    self.send_fetchAll()
    return self.recv_fetchAll()

  def send_fetchAll(self, ):
    self._oprot.writeMessageBegin('fetchAll', TMessageType.CALL, self._seqid)
    args = fetchAll_args()
    args.write(self._oprot)
    self._oprot.writeMessageEnd()
    self._oprot.trans.flush()

  def recv_fetchAll(self, ):
    (fname, mtype, rseqid) = self._iprot.readMessageBegin()
    if mtype == TMessageType.EXCEPTION:
      x = TApplicationException()
      x.read(self._iprot)
      self._iprot.readMessageEnd()
      raise x
    result = fetchAll_result()
    result.read(self._iprot)
    self._iprot.readMessageEnd()
    if result.success != None:
      return result.success
    if result.ex != None:
      raise result.ex
    raise TApplicationException(TApplicationException.MISSING_RESULT, "fetchAll failed: unknown result");

  def getSchema(self, ):
    self.send_getSchema()
    return self.recv_getSchema()

  def send_getSchema(self, ):
    self._oprot.writeMessageBegin('getSchema', TMessageType.CALL, self._seqid)
    args = getSchema_args()
    args.write(self._oprot)
    self._oprot.writeMessageEnd()
    self._oprot.trans.flush()

  def recv_getSchema(self, ):
    (fname, mtype, rseqid) = self._iprot.readMessageBegin()
    if mtype == TMessageType.EXCEPTION:
      x = TApplicationException()
      x.read(self._iprot)
      self._iprot.readMessageEnd()
      raise x
    result = getSchema_result()
    result.read(self._iprot)
    self._iprot.readMessageEnd()
    if result.success != None:
      return result.success
    if result.ex != None:
      raise result.ex
    raise TApplicationException(TApplicationException.MISSING_RESULT, "getSchema failed: unknown result");


class Processor(hive_metastore.ThriftHiveMetastore.Processor, Iface, TProcessor):
  def __init__(self, handler):
    hive_metastore.ThriftHiveMetastore.Processor.__init__(self, handler)
    self._processMap["execute"] = Processor.process_execute
    self._processMap["fetchOne"] = Processor.process_fetchOne
    self._processMap["fetchN"] = Processor.process_fetchN
    self._processMap["fetchAll"] = Processor.process_fetchAll
    self._processMap["getSchema"] = Processor.process_getSchema

  def process(self, iprot, oprot):
    (name, type, seqid) = iprot.readMessageBegin()
    if name not in self._processMap:
      iprot.skip(TType.STRUCT)
      iprot.readMessageEnd()
      x = TApplicationException(TApplicationException.UNKNOWN_METHOD, 'Unknown function %s' % (name))
      oprot.writeMessageBegin(name, TMessageType.EXCEPTION, seqid)
      x.write(oprot)
      oprot.writeMessageEnd()
      oprot.trans.flush()
      return
    else:
      self._processMap[name](self, seqid, iprot, oprot)
    return True

  def process_execute(self, seqid, iprot, oprot):
    args = execute_args()
    args.read(iprot)
    iprot.readMessageEnd()
    result = execute_result()
    try:
      self._handler.execute(args.query)
    except HiveServerException, ex:
      result.ex = ex
    oprot.writeMessageBegin("execute", TMessageType.REPLY, seqid)
    result.write(oprot)
    oprot.writeMessageEnd()
    oprot.trans.flush()

  def process_fetchOne(self, seqid, iprot, oprot):
    args = fetchOne_args()
    args.read(iprot)
    iprot.readMessageEnd()
    result = fetchOne_result()
    try:
      result.success = self._handler.fetchOne()
    except HiveServerException, ex:
      result.ex = ex
    oprot.writeMessageBegin("fetchOne", TMessageType.REPLY, seqid)
    result.write(oprot)
    oprot.writeMessageEnd()
    oprot.trans.flush()

  def process_fetchN(self, seqid, iprot, oprot):
    args = fetchN_args()
    args.read(iprot)
    iprot.readMessageEnd()
    result = fetchN_result()
    try:
      result.success = self._handler.fetchN(args.numRows)
    except HiveServerException, ex:
      result.ex = ex
    oprot.writeMessageBegin("fetchN", TMessageType.REPLY, seqid)
    result.write(oprot)
    oprot.writeMessageEnd()
    oprot.trans.flush()

  def process_fetchAll(self, seqid, iprot, oprot):
    args = fetchAll_args()
    args.read(iprot)
    iprot.readMessageEnd()
    result = fetchAll_result()
    try:
      result.success = self._handler.fetchAll()
    except HiveServerException, ex:
      result.ex = ex
    oprot.writeMessageBegin("fetchAll", TMessageType.REPLY, seqid)
    result.write(oprot)
    oprot.writeMessageEnd()
    oprot.trans.flush()

  def process_getSchema(self, seqid, iprot, oprot):
    args = getSchema_args()
    args.read(iprot)
    iprot.readMessageEnd()
    result = getSchema_result()
    try:
      result.success = self._handler.getSchema()
    except HiveServerException, ex:
      result.ex = ex
    oprot.writeMessageBegin("getSchema", TMessageType.REPLY, seqid)
    result.write(oprot)
    oprot.writeMessageEnd()
    oprot.trans.flush()


# HELPER FUNCTIONS AND STRUCTURES

class execute_args:

  thrift_spec = (
    None, # 0
    (1, TType.STRING, 'query', None, None, ), # 1
  )

  def __init__(self, d=None):
    self.query = None
    if isinstance(d, dict):
      if 'query' in d:
        self.query = d['query']

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRING:
          self.query = iprot.readString();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('execute_args')
    if self.query != None:
      oprot.writeFieldBegin('query', TType.STRING, 1)
      oprot.writeString(self.query)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class execute_result:

  thrift_spec = (
    None, # 0
    (1, TType.STRUCT, 'ex', (HiveServerException, HiveServerException.thrift_spec), None, ), # 1
  )

  def __init__(self, d=None):
    self.ex = None
    if isinstance(d, dict):
      if 'ex' in d:
        self.ex = d['ex']

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRUCT:
          self.ex = HiveServerException()
          self.ex.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('execute_result')
    if self.ex != None:
      oprot.writeFieldBegin('ex', TType.STRUCT, 1)
      self.ex.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class fetchOne_args:

  thrift_spec = (
  )

  def __init__(self, d=None):
    pass

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('fetchOne_args')
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class fetchOne_result:

  thrift_spec = (
    (0, TType.STRING, 'success', None, None, ), # 0
    (1, TType.STRUCT, 'ex', (HiveServerException, HiveServerException.thrift_spec), None, ), # 1
  )

  def __init__(self, d=None):
    self.success = None
    self.ex = None
    if isinstance(d, dict):
      if 'success' in d:
        self.success = d['success']
      if 'ex' in d:
        self.ex = d['ex']

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 0:
        if ftype == TType.STRING:
          self.success = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 1:
        if ftype == TType.STRUCT:
          self.ex = HiveServerException()
          self.ex.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('fetchOne_result')
    if self.success != None:
      oprot.writeFieldBegin('success', TType.STRING, 0)
      oprot.writeString(self.success)
      oprot.writeFieldEnd()
    if self.ex != None:
      oprot.writeFieldBegin('ex', TType.STRUCT, 1)
      self.ex.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class fetchN_args:

  thrift_spec = (
    None, # 0
    (1, TType.I32, 'numRows', None, None, ), # 1
  )

  def __init__(self, d=None):
    self.numRows = None
    if isinstance(d, dict):
      if 'numRows' in d:
        self.numRows = d['numRows']

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.I32:
          self.numRows = iprot.readI32();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('fetchN_args')
    if self.numRows != None:
      oprot.writeFieldBegin('numRows', TType.I32, 1)
      oprot.writeI32(self.numRows)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class fetchN_result:

  thrift_spec = (
    (0, TType.LIST, 'success', (TType.STRING,None), None, ), # 0
    (1, TType.STRUCT, 'ex', (HiveServerException, HiveServerException.thrift_spec), None, ), # 1
  )

  def __init__(self, d=None):
    self.success = None
    self.ex = None
    if isinstance(d, dict):
      if 'success' in d:
        self.success = d['success']
      if 'ex' in d:
        self.ex = d['ex']

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 0:
        if ftype == TType.LIST:
          self.success = []
          (_etype3, _size0) = iprot.readListBegin()
          for _i4 in xrange(_size0):
            _elem5 = iprot.readString();
            self.success.append(_elem5)
          iprot.readListEnd()
        else:
          iprot.skip(ftype)
      elif fid == 1:
        if ftype == TType.STRUCT:
          self.ex = HiveServerException()
          self.ex.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('fetchN_result')
    if self.success != None:
      oprot.writeFieldBegin('success', TType.LIST, 0)
      oprot.writeListBegin(TType.STRING, len(self.success))
      for iter6 in self.success:
        oprot.writeString(iter6)
      oprot.writeListEnd()
      oprot.writeFieldEnd()
    if self.ex != None:
      oprot.writeFieldBegin('ex', TType.STRUCT, 1)
      self.ex.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class fetchAll_args:

  thrift_spec = (
  )

  def __init__(self, d=None):
    pass

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('fetchAll_args')
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class fetchAll_result:

  thrift_spec = (
    (0, TType.LIST, 'success', (TType.STRING,None), None, ), # 0
    (1, TType.STRUCT, 'ex', (HiveServerException, HiveServerException.thrift_spec), None, ), # 1
  )

  def __init__(self, d=None):
    self.success = None
    self.ex = None
    if isinstance(d, dict):
      if 'success' in d:
        self.success = d['success']
      if 'ex' in d:
        self.ex = d['ex']

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 0:
        if ftype == TType.LIST:
          self.success = []
          (_etype10, _size7) = iprot.readListBegin()
          for _i11 in xrange(_size7):
            _elem12 = iprot.readString();
            self.success.append(_elem12)
          iprot.readListEnd()
        else:
          iprot.skip(ftype)
      elif fid == 1:
        if ftype == TType.STRUCT:
          self.ex = HiveServerException()
          self.ex.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('fetchAll_result')
    if self.success != None:
      oprot.writeFieldBegin('success', TType.LIST, 0)
      oprot.writeListBegin(TType.STRING, len(self.success))
      for iter13 in self.success:
        oprot.writeString(iter13)
      oprot.writeListEnd()
      oprot.writeFieldEnd()
    if self.ex != None:
      oprot.writeFieldBegin('ex', TType.STRUCT, 1)
      self.ex.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class getSchema_args:

  thrift_spec = (
  )

  def __init__(self, d=None):
    pass

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('getSchema_args')
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class getSchema_result:

  thrift_spec = (
    (0, TType.STRING, 'success', None, None, ), # 0
    (1, TType.STRUCT, 'ex', (HiveServerException, HiveServerException.thrift_spec), None, ), # 1
  )

  def __init__(self, d=None):
    self.success = None
    self.ex = None
    if isinstance(d, dict):
      if 'success' in d:
        self.success = d['success']
      if 'ex' in d:
        self.ex = d['ex']

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 0:
        if ftype == TType.STRING:
          self.success = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 1:
        if ftype == TType.STRUCT:
          self.ex = HiveServerException()
          self.ex.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('getSchema_result')
    if self.success != None:
      oprot.writeFieldBegin('success', TType.STRING, 0)
      oprot.writeString(self.success)
      oprot.writeFieldEnd()
    if self.ex != None:
      oprot.writeFieldBegin('ex', TType.STRUCT, 1)
      self.ex.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def __str__(self):
    return str(self.__dict__)

  def __repr__(self):
    return repr(self.__dict__)

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)


