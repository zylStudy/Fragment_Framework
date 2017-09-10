package com.makeryan.lib.fragment;

import android.app.Activity;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.makeryan.lib.event.EventBean;
import com.makeryan.lib.fragment.fragmentation.SupportFragment;
import com.makeryan.lib.fragment.fragmentation_swipeback.SwipeBackFragment;
import com.makeryan.lib.mvp.presenter.BasePresenter;
import com.makeryan.lib.photopicker.utils.PermissionsUtils;
import com.makeryan.lib.util.GlobUtils;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;
import com.zhy.m.permission.ShowRequestPermissionRationale;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;

import com.makeryan.lib.R;


/**
 * Created by MakerYan on 16/3/31 17:13.
 * Email: light.yan@qq.com
 */
public abstract class BaseFragment
		extends SwipeBackFragment
		implements View.OnClickListener {

	protected Gson mGson = new Gson();

	protected Bundle mExtras;

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		EventBus.getDefault()
				.register(this);
		Bundle extras = getArguments();
		if (null != extras) {
			getExtras(extras);
		}
		BasePresenter presenter = getPresenter();
		if (presenter != null && extras != null) {
			presenter.setExtras(extras);
		}
		beforeSetContentView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = null;
		if (getLayoutResID() != 0) {
			ViewDataBinding binding = initDataBinding(
					inflater,
					container
													 );
			if (binding == null) {
				view = inflater.inflate(
						getLayoutResID(),
						container,
						false
									   );
			} else {
				view = binding.getRoot();
			}
		} else {
			view = super.onCreateView(
					inflater,
					container,
					savedInstanceState
									 );
		}
		if (getToolbar(view) != null) {
			_mActivity.setSupportActionBar(getToolbar(view));
		}
		return view;
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public void onLazyInitView(@Nullable Bundle savedInstanceState) {

		super.onLazyInitView(savedInstanceState);
		View view = getView();
		//这里已经做了返回键的处理
		Toolbar toolbar = getToolbar(view);
		if (toolbar != null) {
			toolbar.setTitle("");
			toolbar.setNavigationOnClickListener(v -> {
				SupportFragment topFragment = getTopFragment();
				if (topFragment != null) {
					BasePresenter presenter = topFragment.getPresenter();
					if (presenter != null && presenter.onBackPressedSupport()) {
						getSupportActivity().onBackPressed();
					} else {
						getSupportActivity().onBackPressed();
					}
				} else {
					getSupportActivity().onBackPressed();
				}
			});
		}
		assignViews(view);
		registerListeners();
		doAction();
	}

	@Override
	public void onSupportVisible() {

		super.onSupportVisible();
		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.onSupportVisible(this);
		}
	}

	@Override
	public void onSupportInvisible() {

		super.onSupportInvisible();
		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.onSupportInvisible(this);
		}
	}

	@Override
	public boolean onBackPressedSupport() {

		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			return presenter.onBackPressedSupport();
		}
		return super.onBackPressedSupport();
	}

	@Override
	public void setFragmentResult(int resultCode, Bundle bundle) {

		super.setFragmentResult(
				resultCode,
				bundle
							   );
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.onRequestPermissionsResult(
					requestCode,
					permissions,
					grantResults
												);
		}
		super.onRequestPermissionsResult(
				requestCode,
				permissions,
				grantResults
										);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		super.onSaveInstanceState(savedInstanceState);

		SupportFragment topFragment = getTopFragment();
		if (topFragment != null && topFragment != this) {
			topFragment.onSaveInstanceState(savedInstanceState);
		}

		SupportFragment topChildFragment = getTopChildFragment();
		if (topChildFragment != null && topChildFragment != this) {
			topChildFragment.onSaveInstanceState(savedInstanceState);
		}

		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.onSaveInstanceState(savedInstanceState);
		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {

		super.onViewStateRestored(savedInstanceState);

		SupportFragment topFragment = getTopFragment();
		if (topFragment != null && topFragment != this) {
			topFragment.onViewStateRestored(savedInstanceState);
		}

		SupportFragment topChildFragment = getTopChildFragment();
		if (topChildFragment != null && topChildFragment != this) {
			topChildFragment.onViewStateRestored(savedInstanceState);
		}
		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.onViewStateRestored(savedInstanceState);
		}
	}


	@Override
	public void pop() {

		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.setFragmentResult();
		}
		super.pop();
	}

	@Override
	public void popTo(String targetFragmentTag, boolean includeTargetFragment, Runnable afterPopTransactionRunnable) {

		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.setFragmentResult();
		}
		super.popTo(
				targetFragmentTag,
				includeTargetFragment,
				afterPopTransactionRunnable
				   );
	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
		if (getPresenter() != null) {
			getPresenter().detachView(true);
		}
	}

	@Override
	public void onDestroy() {

		EventBus.getDefault()
				.unregister(this);
		if (getPresenter() != null) {
			getPresenter().destroy();
		}
		GlobUtils.setViewBgAsNull((ViewGroup) getView());
		super.onDestroy();
	}

	@Override
	public void onDetach() {

		super.onDetach();
		// for bug ---> java.lang.IllegalStateException: Activity has been destroyed
		try {
			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(
					this,
					null
									);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (getPresenter() != null) {
			getPresenter().onActivityResult(
					requestCode,
					resultCode,
					data
										   );
		}
		super.onActivityResult(
				requestCode,
				resultCode,
				data
							  );
	}

	@Override
	public void onFragmentResult(int requestCode, int resultCode, Bundle data) {

		SupportFragment topFragment = getTopFragment();
		if (topFragment != null && topFragment != this) {
			topFragment.onFragmentResult(
					requestCode,
					resultCode,
					data
										);
		}

		SupportFragment topChildFragment = getTopChildFragment();
		if (topChildFragment != null && topChildFragment != this) {
			topChildFragment.onFragmentResult(
					requestCode,
					resultCode,
					data
											 );
		}

		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.onFragmentResult(
					requestCode,
					resultCode,
					data
									  );
		}
		super.onFragmentResult(
				requestCode,
				resultCode,
				data
							  );
	}

	/**
	 * @return 初始化并返回当前Presenter
	 */
	public abstract BasePresenter getPresenter();

	/**
	 * 加载布局之前调用
	 */

	protected void beforeSetContentView() {

	}

	/**
	 * @param extras
	 * 		上层传过来的数据
	 */
	protected void getExtras(Bundle extras) {

		mExtras = extras;
	}

	/**
	 * @return 布局Id
	 */
	protected abstract int getLayoutResID();

	protected void processonCreateViewSavedInstanceState(Bundle savedInstanceState) {

		BasePresenter presenter = getPresenter();
		if (presenter != null) {
			presenter.processonCreateViewSavedInstanceState(savedInstanceState);
		}
	}

	/**
	 * 初始化DataBinding
	 *
	 * @param inflater
	 * @param parent
	 */
	protected abstract ViewDataBinding initDataBinding(LayoutInflater inflater, ViewGroup parent);

	/**
	 * @return 获取当前Toolbar
	 */
	protected Toolbar getToolbar(View view) {

		if (view == null) {
			return null;
		}
		View toolbar = view.findViewById(R.id.toolbar);
		if (toolbar != null) {
			return (Toolbar) toolbar;
		} else {
			return null;
		}
	}

	/**
	 * 获取控件
	 */
	protected void assignViews(View view) {


	}

	/**
	 * 注册控件监听
	 */
	protected void registerListeners() {


	}

	/**
	 * 开始处理
	 */
	protected abstract void doAction();

	/**
	 * @param event
	 * 		eventbus
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventBus(EventBean event) {

	}

	/**
	 * @param event
	 * 		eventbus
	 */
	@Subscribe(threadMode = ThreadMode.POSTING)
	public void onEventBusByPosting(EventBean event) {

	}

	/**
	 * @param event
	 * 		eventbus
	 */
	@Subscribe(threadMode = ThreadMode.BACKGROUND)
	public void onEventBusByBackground(EventBean event) {

	}

	/**
	 * @param event
	 * 		eventbus
	 */
	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onEventBusByAsync(EventBean event) {

	}

	/**
	 * Called when a view has been clicked.
	 *
	 * @param v
	 * 		The view that was clicked.
	 */
	@Override
	public void onClick(View v) {

	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GROUP_CALENDAR)
	public void whyNeedGroupCalendar() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GROUP_CALENDAR,
				PermissionsUtils.GROUP_CALENDAR
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GROUP_CAMERA)
	public void whyNeedGroupCamera() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GROUP_CAMERA,
				PermissionsUtils.GROUP_CAMERA
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GROUP_CONTACTS)
	public void whyNeedGroupContacts() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GROUP_CONTACTS,
				PermissionsUtils.GROUP_CONTACTS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GROUP_MICROPHONE)
	public void whyNeedGroupMicrophone() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GROUP_MICROPHONE,
				PermissionsUtils.GROUP_MICROPHONE
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GROUP_PHONE)
	public void whyNeedGroupPhone() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GROUP_PHONE,
				PermissionsUtils.GROUP_PHONE
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GROUP_SENSORS)
	public void whyNeedGroupSensors() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GROUP_SENSORS,
				PermissionsUtils.GROUP_SENSORS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GROUP_SMS)
	public void whyNeedGroupSms() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GROUP_SMS,
				PermissionsUtils.GROUP_SMS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GROUP_STORAGE)
	public void whyNeedGroupStorage() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GROUP_STORAGE,
				PermissionsUtils.GROUP_STORAGE
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_WRITE_CONTACTS)
	public void whyNeedWriteContacts() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_WRITE_CONTACTS,
				PermissionsUtils.WRITE_CONTACTS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_GET_ACCOUNTS)
	public void whyNeedGetAccounts() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_GET_ACCOUNTS,
				PermissionsUtils.GET_ACCOUNTS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_READ_CONTACTS)
	public void whyNeedReadContacts() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_READ_CONTACTS,
				PermissionsUtils.READ_CONTACTS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_READ_CALL_LOG)
	public void whyNeedReadCallLog() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_READ_CALL_LOG,
				PermissionsUtils.READ_CALL_LOG
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_READ_PHONE_STATE)
	public void whyNeedReadPhoneState() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_READ_PHONE_STATE,
				PermissionsUtils.READ_PHONE_STATE
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_CALL_PHONE)
	public void whyNeedCallPhone() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_CALL_PHONE,
				PermissionsUtils.CALL_PHONE
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_WRITE_CALL_LOG)
	public void whyNeedWriteCallLog() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_WRITE_CALL_LOG,
				PermissionsUtils.WRITE_CALL_LOG
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_USE_SIP)
	public void whyNeedUseSip() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_USE_SIP,
				PermissionsUtils.USE_SIP
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_PROCESS_OUTGOING_CALLS)
	public void whyNeedProcessOutgoingCalls() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_PROCESS_OUTGOING_CALLS,
				PermissionsUtils.PROCESS_OUTGOING_CALLS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_ADD_VOICEMAIL)
	public void whyNeedAddVoicemail() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_ADD_VOICEMAIL,
				PermissionsUtils.ADD_VOICEMAIL
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_READ_CALENDAR)
	public void whyNeedReadCalendar() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_READ_CALENDAR,
				PermissionsUtils.READ_CALENDAR
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_WRITE_CALENDAR)
	public void whyNeedWriteCalendar() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_WRITE_CALENDAR,
				PermissionsUtils.WRITE_CALENDAR
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_CAMERA)
	public void whyNeedCamera() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_CAMERA,
				PermissionsUtils.CAMERA
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_BODY_SENSORS)
	public void whyNeedBodySensors() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_BODY_SENSORS,
				PermissionsUtils.BODY_SENSORS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_ACCESS_FINE_LOCATION)
	public void whyNeedAccessFineLocation() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_ACCESS_FINE_LOCATION,
				PermissionsUtils.ACCESS_FINE_LOCATION
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_ACCESS_COARSE_LOCATION)
	public void whyNeedAccessCoarseLocation() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_ACCESS_COARSE_LOCATION,
				PermissionsUtils.ACCESS_COARSE_LOCATION
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_READ_EXTERNAL_STORAGE)
	public void whyNeedReadExternalStorage() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_READ_EXTERNAL_STORAGE,
				PermissionsUtils.READ_EXTERNAL_STORAGE
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
	public void whyNeedWriteExternalStorage() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_WRITE_EXTERNAL_STORAGE,
				PermissionsUtils.WRITE_EXTERNAL_STORAGE
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_RECORD_AUDIO)
	public void whyNeedRecordAudio() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_RECORD_AUDIO,
				PermissionsUtils.RECORD_AUDIO
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_READ_SMS)
	public void whyNeedReadSms() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_READ_SMS,
				PermissionsUtils.READ_SMS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_RECEIVE_WAP_PUSH)
	public void whyNeedReceiveWapPush() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_RECEIVE_WAP_PUSH,
				PermissionsUtils.RECEIVE_WAP_PUSH
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_RECEIVE_MMS)
	public void whyNeedReceiveMms() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_RECEIVE_MMS,
				PermissionsUtils.RECEIVE_MMS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_RECEIVE_SMS)
	public void whyNeedReceiveSms() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_RECEIVE_SMS,
				PermissionsUtils.RECEIVE_SMS
									   );
	}

	@ShowRequestPermissionRationale(PermissionsUtils.REQUEST_CODE_SEND_SMS)
	public void whyNeedSendSms() {

		MPermissions.requestPermissions(
				this,
				PermissionsUtils.REQUEST_CODE_SEND_SMS,
				PermissionsUtils.SEND_SMS
									   );
	}


	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GROUP_CALENDAR)
	public void requestGroupCalendarSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GROUP_CAMERA)
	public void requestGroupCameraSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GROUP_CONTACTS)
	public void requestGroupContactsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GROUP_MICROPHONE)
	public void requestGroupMicrophoneSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GROUP_PHONE)
	public void requestGroupPhoneSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GROUP_SENSORS)
	public void requestGroupSensorsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GROUP_SMS)
	public void requestGroupSmsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GROUP_STORAGE)
	public void requestGroupStorageSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_WRITE_CONTACTS)
	public void requestWriteContactsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_GET_ACCOUNTS)
	public void requestGetAccountsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_READ_CONTACTS)
	public void requestReadContactsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_READ_CALL_LOG)
	public void requestReadCallLogSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_READ_PHONE_STATE)
	public void requestReadPhoneStateSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_CALL_PHONE)
	public void requestCallPhoneSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_WRITE_CALL_LOG)
	public void requestWriteCallLogSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_USE_SIP)
	public void requestUseSipSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_PROCESS_OUTGOING_CALLS)
	public void requestProcessOutgoingCallsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_ADD_VOICEMAIL)
	public void requestAddVoicemailSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_READ_CALENDAR)
	public void requestReadCalendarSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_WRITE_CALENDAR)
	public void requestWriteCalendarSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_CAMERA)
	public void requestCameraSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_BODY_SENSORS)
	public void requestBodySensorsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_ACCESS_FINE_LOCATION)
	public void requestAccessFineLocationSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_ACCESS_COARSE_LOCATION)
	public void requestAccessCoarseLocationSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_READ_EXTERNAL_STORAGE)
	public void requestReadExternalStorageSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
	public void requestWriteExternalStorageSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_RECORD_AUDIO)
	public void requestRecordAudioSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_READ_SMS)
	public void requestReadSmsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_RECEIVE_WAP_PUSH)
	public void requestReceiveWapPushSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_RECEIVE_MMS)
	public void requestReceiveMmsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_RECEIVE_SMS)
	public void requestReceiveSmsSuccess() {

	}

	@PermissionGrant(PermissionsUtils.REQUEST_CODE_SEND_SMS)
	public void requestSendSmsSuccess() {

	}


	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GROUP_CALENDAR)
	public void requestGroupCalendarFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GROUP_CAMERA)
	public void requestGroupCameraFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GROUP_CONTACTS)
	public void requestGroupContactsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GROUP_MICROPHONE)
	public void requestGroupMicrophoneFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GROUP_PHONE)
	public void requestGroupPhoneFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GROUP_SENSORS)
	public void requestGroupSensorsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GROUP_SMS)
	public void requestGroupSmsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GROUP_STORAGE)
	public void requestGroupStorageFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_WRITE_CONTACTS)
	public void requestWriteContactsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_GET_ACCOUNTS)
	public void requestGetAccountsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_READ_CONTACTS)
	public void requestReadContactsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_READ_CALL_LOG)
	public void requestReadCallLogFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_READ_PHONE_STATE)
	public void requestReadPhoneStateFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_CALL_PHONE)
	public void requestCallPhoneFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_WRITE_CALL_LOG)
	public void requestWriteCallLogFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_USE_SIP)
	public void requestUseSipFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_PROCESS_OUTGOING_CALLS)
	public void requestProcessOutgoingCallsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_ADD_VOICEMAIL)
	public void requestAddVoicemailFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_READ_CALENDAR)
	public void requestReadCalendarFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_WRITE_CALENDAR)
	public void requestWriteCalendarFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_CAMERA)
	public void requestCameraFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_BODY_SENSORS)
	public void requestBodySensorsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_ACCESS_FINE_LOCATION)
	public void requestAccessFineLocationFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_ACCESS_COARSE_LOCATION)
	public void requestAccessCoarseLocationFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_READ_EXTERNAL_STORAGE)
	public void requestReadExternalStorageFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
	public void requestWriteExternalStorageFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_RECORD_AUDIO)
	public void requestRecordAudioFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_READ_SMS)
	public void requestReadSmsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_RECEIVE_WAP_PUSH)
	public void requestReceiveWapPushFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_RECEIVE_MMS)
	public void requestReceiveMmsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_RECEIVE_SMS)
	public void requestReceiveSmsFailed() {

	}

	@PermissionDenied(PermissionsUtils.REQUEST_CODE_SEND_SMS)
	public void requestSendSmsFailed() {

	}
}
