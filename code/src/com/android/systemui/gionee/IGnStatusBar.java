/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/com/android/internal/statusbar/IGnStatusBar.aidl
 */
package com.android.internal.statusbar;
/** @hide */
public interface IGnStatusBar extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.android.internal.statusbar.IGnStatusBar
{
private static final java.lang.String DESCRIPTOR = "com.android.internal.statusbar.IGnStatusBar";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.android.internal.statusbar.IGnStatusBar interface,
 * generating a proxy if needed.
 */
public static com.android.internal.statusbar.IGnStatusBar asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.android.internal.statusbar.IGnStatusBar))) {
return ((com.android.internal.statusbar.IGnStatusBar)iin);
}
return new com.android.internal.statusbar.IGnStatusBar.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
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
case TRANSACTION_onSwipeFromBottom:
{
data.enforceInterface(DESCRIPTOR);
this.onSwipeFromBottom();
reply.writeNoException();
return true;
}
case TRANSACTION_onPointerEvent:
{
data.enforceInterface(DESCRIPTOR);
android.view.MotionEvent _arg0;
if ((0!=data.readInt())) {
_arg0 = android.view.MotionEvent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onPointerEvent(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_showSimIndicator:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.showSimIndicator(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_hideSimIndicator:
{
data.enforceInterface(DESCRIPTOR);
this.hideSimIndicator();
reply.writeNoException();
return true;
}
case TRANSACTION_onNotificationPriorityChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
this.onNotificationPriorityChanged(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.android.internal.statusbar.IGnStatusBar
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onSwipeFromBottom() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onSwipeFromBottom, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onPointerEvent(android.view.MotionEvent event) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((event!=null)) {
_data.writeInt(1);
event.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onPointerEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void showSimIndicator(java.lang.String businessType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(businessType);
mRemote.transact(Stub.TRANSACTION_showSimIndicator, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void hideSimIndicator() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_hideSimIndicator, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onNotificationPriorityChanged(java.lang.String pkg, int priority) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkg);
_data.writeInt(priority);
mRemote.transact(Stub.TRANSACTION_onNotificationPriorityChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onSwipeFromBottom = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onPointerEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_showSimIndicator = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_hideSimIndicator = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onNotificationPriorityChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void onSwipeFromBottom() throws android.os.RemoteException;
public void onPointerEvent(android.view.MotionEvent event) throws android.os.RemoteException;
public void showSimIndicator(java.lang.String businessType) throws android.os.RemoteException;
public void hideSimIndicator() throws android.os.RemoteException;
public void onNotificationPriorityChanged(java.lang.String pkg, int priority) throws android.os.RemoteException;
}
