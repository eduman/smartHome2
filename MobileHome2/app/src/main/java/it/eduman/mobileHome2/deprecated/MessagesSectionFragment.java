//package it.eduman.mobileHome2;
//
//import it.eduman.android.commons.utilities.ActionTask;
//import it.eduman.android.commons.utilities.HardwareUtilities;
//import it.eduman.android.commons.utilities.Response;
//import it.eduman.android.commons.utilities.SoftwareUtilities;
//import it.eduman.android.commons.utilities.TaskOn;
//import it.eduman.mobileHome2.commons.MobileHomeConstants;
//import it.eduman.mobileHome2.communication.ProxyWebServices;
//import it.eduman.smartHome.deprecated.security.SecurityException;
//import it.eduman.smartHome.deprecated.userMessage.MessageContent;
//import it.eduman.smartHome.deprecated.userMessage.UserMessageContent;
//import it.eduman.smartHome.deprecated.webServices.QueryContent;
//
//import android.content.SharedPreferences;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//public class MessagesSectionFragment extends MyFragment {
//
//	private static View rootView = null;
//	private LayoutInflater inflater = null;
//	private SharedPreferences sharedPref;
////	private static UserMessageContent userMessages = null;
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		this.inflater = inflater;
//		rootView = inflater.inflate(R.layout.messages_fragment_activity,
//				container, false);
//
//		sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
//
//		ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.messages_fragment_refresh_button);
//		refreshButton.setVisibility(View.VISIBLE);
//		refreshButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				MyFragment.CURRENT_VISIBLE_FRAGMENT = MobileHomeConstants.MESSAGES_FRAGMENT_POSITION;
//				update();
//			}
//		});
//
//		ImageButton homeButton = (ImageButton)rootView.findViewById(R.id.messages_fragment_home_button);
//		homeButton.setVisibility(View.VISIBLE);
//		homeButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				((MainActivity)getActivity()).nextPage(MobileHomeConstants.MAIN_FRAGMENT_POSITION);
//			}
//		});
//
//		ImageButton clearAllButton = (ImageButton)rootView.findViewById(R.id.messages_fragment_clearAll_button);
//		clearAllButton.setVisibility(View.VISIBLE);
//		clearAllButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				SoftwareUtilities.MyInfoDialogFactory(
//						rootView.getContext(),
//						R.string.messagesClearAll,
//						true,
//						new ActionTask() {
//
//							@Override
//							public void onPositiveResponse() {
//								(new DeleteAllMessagesAsyncTask(rootView)).execute();
//							}
//
//							@Override
//							public void onNeutralResponse() {}
//
//							@Override
//							public void onNegativeResponse() {}
//						});
//			}
//		});
//
//		return rootView;
//	}
//
//	@Override
//	public void onResume(){
//		super.onResume();
//		ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
//		update();
//	}
//
//	@Override
//	public void onPause(){
//		//here my code
//		super.onPause();
//	}
//
//
//	@Override
//	public void update() {
//		if (MyFragment.CURRENT_VISIBLE_FRAGMENT == MobileHomeConstants.MESSAGES_FRAGMENT_POSITION){
//			if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
//				(new LoadMessagesAsyncTask(rootView)).execute();
//			} else {
//				HardwareUtilities.enableInternetConnectionAlertDialog(
//						rootView.getContext(), true, false);
//			}
//		}
//	}
//
//
//	class UserMessageAdapter extends BaseAdapter {
//
//		private UserMessageContent userMessageContent;
//		private MessageContent[] messagesArray;
//
//		public UserMessageAdapter (UserMessageContent userMessageContent) {
//			this.userMessageContent = userMessageContent;
//			toMessageArray();
//		}
//
//		private void toMessageArray(){
//			this.messagesArray =
//					new MessageContent[userMessageContent.getMessageList().size()];
//			userMessageContent.getMessageList().toArray(this.messagesArray);
//		}
//
//		@Override
//		public int getCount() {
//			return this.messagesArray.length;
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return position;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			View view = convertView;
//			final MessageViewHolder holder;
//			if (convertView == null){
//				view = inflater.inflate(R.layout.messages_list_row, parent, false);
//				if ((position % 2) == 0)
//					view.setBackgroundColor(0xFFe3e3e3);
//				holder = new MessageViewHolder();
//				holder.messageID = (TextView) view.findViewById(R.id.messages_row_messageID);
//				holder.text = (TextView) view.findViewById(R.id.messages_row_text);
//				holder.timestamp = (TextView) view.findViewById(R.id.messages_row_date);
//				holder.image = (ImageView) view.findViewById(R.id.messages_row_image);
//				holder.clearButton = (ImageButton)view.findViewById(R.id.messages_row_clearButton);
//				holder.clearButton.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
//							(new DeleteMessageAsyncTask(rootView, holder)).execute();
//						} else {
//							HardwareUtilities.enableInternetConnectionAlertDialog(
//									rootView.getContext(), true, false);
//						}
//					}
//				});
//
//				view.setTag(holder);
//
//			} else {
//				holder = (MessageViewHolder) view.getTag();
//			}
//
//			holder.messageID.setText(String.valueOf(messagesArray[position].getId()));
//
//			if (messagesArray[position].getText() != null){
//				holder.text.setText(messagesArray[position].getText());
//			}
//
//			if (messagesArray[position].getFormattedTimeStamp() != null){
//				holder.timestamp.setText(messagesArray[position].getFormattedTimeStamp());
//			}
//
//			switch (messagesArray[position].getCode()) {
//				case MessageContent.HOME_CODE:
//					holder.image.setImageResource(R.drawable.ic_launcher);
//					break;
//
//				case MessageContent.LIGHT_CODE:
//					holder.image.setImageResource(R.drawable.ic_lamp);
//					break;
//
//				case MessageContent.HEATING_CODE:
//					holder.image.setImageResource(R.drawable.ic_thermometer);
//					break;
//
//				case MessageContent.COOLING_CODE:
//					holder.image.setImageResource(R.drawable.ic_snow);
//					break;
//
////				case MessageContent.DEVICE_CODE:
////					holder.image.setImageResource(R.drawable);
////					break;
//
//				case MessageContent.GENERIC_INFO_CODE:
//				default:
//					holder.image.setImageResource(R.drawable.ic_info);
//					break;
//			}
//
//
//			return view;
//		}
//
//	}
//
//	class MessageViewHolder {
//		public TextView messageID;
//		public TextView text;
//		public TextView timestamp;
//		public ImageView image;
//		public ImageButton clearButton;
//	}
//
//	class DeleteMessageAsyncTask extends AsyncTask<Void, Void, Response<UserMessageContent>>{
//		private View view;
//		private ProgressBar progressBar;
//		private MessageViewHolder holder = null;
//
//		public DeleteMessageAsyncTask (View view, MessageViewHolder holder){
//			this.view = view;
//			this.holder = holder;
//			progressBar = (ProgressBar) this.view.findViewById(R.id.messageActivity_progressBar);
//		}
//
//		@Override
//		protected void onPreExecute(){
//			progressBar.setVisibility(View.VISIBLE);
//		}
//
//
//		@SuppressWarnings("unchecked")
//		@Override
//		protected Response<UserMessageContent> doInBackground(Void... params) {
//			long idToDelete = Long.parseLong(holder.messageID.getText().toString());
//			Response<UserMessageContent> remoteUserMessageContent = null;
//			ProxyWebServices shws = new ProxyWebServices(rootView.getContext(), true);
//			try {
//				QueryContent queryContent = new QueryContent()
//					.setUsername(DefaultUser.getDefaultUsername(rootView.getContext()))
//					.setMessageLongID(idToDelete);
//				remoteUserMessageContent = (Response<UserMessageContent>) shws.deleteUserMessage(
//						queryContent,
//						new TaskOn<Response<UserMessageContent>>() {
//
//							@Override
//							public Object doTask(Response<UserMessageContent> parameter) {
//								return parameter;
//							}
//						});
//			} catch (SecurityException e) {
//				remoteUserMessageContent =  Response.createErrorResponse(e);
//			}
//
//
//			return remoteUserMessageContent;
//		}
//
//		@Override
//		protected void onPostExecute (Response<UserMessageContent> result){
//			loadMessageHolder(result);
//			progressBar.setVisibility(View.INVISIBLE);
//		}
//
//	}
//
//
//	class DeleteAllMessagesAsyncTask extends AsyncTask<Void, Void, Response<UserMessageContent>>{
//
//		private View view;
//		private ProgressBar progressBar;
//
//		public DeleteAllMessagesAsyncTask (View view){
//			this.view = view;
//			progressBar = (ProgressBar) this.view.findViewById(R.id.messageActivity_progressBar);
//		}
//
//		@Override
//		protected void onPreExecute(){
//			progressBar.setVisibility(View.VISIBLE);
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		protected Response<UserMessageContent> doInBackground(Void... params) {
//			Response<UserMessageContent> remoteUserMessageContent = null;
//			ProxyWebServices shws = new ProxyWebServices(rootView.getContext(), true);
//			try {
//				QueryContent queryContent = new QueryContent()
//					.setUsername(DefaultUser.getDefaultUsername(rootView.getContext()));
//				remoteUserMessageContent = (Response<UserMessageContent>) shws.deleteAllUserMessages(
//						queryContent,
//						new TaskOn<Response<UserMessageContent>>() {
//
//							@Override
//							public Object doTask(Response<UserMessageContent> parameter) {
//								return parameter;
//							}
//						});
//			} catch (SecurityException e) {
//				remoteUserMessageContent =  Response.createErrorResponse(e);
//			}
//
//
//			return remoteUserMessageContent;
//		}
//
//		@Override
//		protected void onPostExecute (Response<UserMessageContent> result){
//			loadMessageHolder(result);
//			progressBar.setVisibility(View.INVISIBLE);
//		}
//
//	}
//
//
//	class LoadMessagesAsyncTask extends AsyncTask<Void, Void, Response<UserMessageContent>>{
//
//		private View view;
//		private ProgressBar progressBar;
//
//		public LoadMessagesAsyncTask (View view){
//			this.view = view;
//			progressBar = (ProgressBar) this.view.findViewById(R.id.messageActivity_progressBar);
//		}
//
//		@Override
//		protected void onPreExecute(){
//			progressBar.setVisibility(View.VISIBLE);
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		protected Response<UserMessageContent> doInBackground(Void... params) {
//			Response<UserMessageContent> remoteUserMessageContent = null;
//			ProxyWebServices shws = new ProxyWebServices(rootView.getContext(), true);
//			try {
//				QueryContent queryContent = new QueryContent()
//					.setUsername(DefaultUser.getDefaultUsername(rootView.getContext()));
//				remoteUserMessageContent = (Response<UserMessageContent>) shws.getUserMessages(
//						queryContent,
//						new TaskOn<Response<UserMessageContent>>() {
//
//							@Override
//							public Object doTask(Response<UserMessageContent> parameter) {
//								return parameter;
//							}
//						});
//			} catch (SecurityException e) {
//				remoteUserMessageContent =  Response.createErrorResponse(e);
//			}
//
//
//			return remoteUserMessageContent;
//		}
//
//		@Override
//		protected void onPostExecute (Response<UserMessageContent> result){
//			loadMessageHolder(result);
//			progressBar.setVisibility(View.INVISIBLE);
//		}
//
//	}
//
//	private void loadMessageHolder(Response<UserMessageContent> result){
//		if (result != null){
//			if (result.isOk()){
//				UserMessageContent userMessages = result.getContent();
//				UserMessageAdapter adapter = new UserMessageAdapter(userMessages);
//				adapter.notifyDataSetChanged();
//				ListView listView = (ListView)rootView.findViewById(R.id.messages_ListView);
//				listView.setAdapter(adapter);
//				listView.setClickable(true);
//				listView.setItemsCanFocus(false);
//				if (userMessages.getMessageList().size() == 0){
//					SoftwareUtilities.MyInfoDialogFactory(
//							rootView.getContext(),
//							String.format(rootView.getContext().getResources().getString(R.string.noMessagesForUser),
//									DefaultUser.getDefaultUsername(rootView.getContext())));
//				}
//			} else {
//				SoftwareUtilities.MyErrorDialogFactory(
//						rootView.getContext(),
//						result.getErrorMessage());
//			}
//		}
//
//	}
//}
