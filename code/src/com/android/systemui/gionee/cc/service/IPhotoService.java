/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/wangli/env/workspace4.0/Amigo_shortcuttools_2/src/com/gionee/navil/shortcuttools/service/IPhotoService.aidl
 */
package com.android.systemui.gionee.cc.service;
public interface IPhotoService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.android.systemui.gionee.cc.service.IPhotoService
{
private static final java.lang.String DESCRIPTOR = "com.gionee.navil.shortcuttools.service.IPhotoService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.gionee.navil.shortcuttools.service.IPhotoService interface,
 * generating a proxy if needed.
 */
public static com.android.systemui.gionee.cc.service.IPhotoService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.android.systemui.gionee.cc.service.IPhotoService))) {
return ((com.android.systemui.gionee.cc.service.IPhotoService)iin);
}
return new com.android.systemui.gionee.cc.service.IPhotoService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_savePic:
{
data.enforceInterface(DESCRIPTOR);
int[] _arg0;
_arg0 = data.createIntArray();
byte[] _arg1;
_arg1 = data.createByteArray();
android.net.Uri _result = this.savePic(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_isSdCardOK:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isSdCardOK();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.android.systemui.gionee.cc.service.IPhotoService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public android.net.Uri savePic(int[] degree, byte[] data) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.net.Uri _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeIntArray(degree);
_data.writeByteArray(data);
mRemote.transact(Stub.TRANSACTION_savePic, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.net.Uri.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean isSdCardOK() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isSdCardOK, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_savePic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_isSdCardOK = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public android.net.Uri savePic(int[] degree, byte[] data) throws android.os.RemoteException;
public boolean isSdCardOK() throws android.os.RemoteException;
}
