package com.genonbeta.TrebleShot.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.app.RecyclerViewFragment;
import com.genonbeta.TrebleShot.object.Shareable;
import com.genonbeta.TrebleShot.util.AppUtils;
import com.genonbeta.TrebleShot.util.SweetImageLoader;
import com.genonbeta.TrebleShot.widget.RecyclerViewAdapter;
import com.genonbeta.TrebleShot.widget.ShareableListAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * created by: Veli
 * date: 18.11.2017 13:32
 */

public class ImageListAdapter
		extends ShareableListAdapter<ImageListAdapter.ImageHolder, RecyclerViewAdapter.ViewHolder>
		implements SweetImageLoader.Handler<ImageListAdapter.ImageHolder, Bitmap>
{
	private ContentResolver mResolver;
	private Bitmap mDefaultImageBitmap;

	public ImageListAdapter(Context context)
	{
		super(context);

		mResolver = context.getContentResolver();
		mDefaultImageBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_refresh_white_24dp);
	}

	@Override
	public Bitmap onLoadBitmap(ImageHolder object)
	{
		Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(mResolver, object.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
		return bitmap == null ? mDefaultImageBitmap : bitmap;
	}

	@Override
	public ArrayList<ImageHolder> onLoad()
	{
		ArrayList<ImageHolder> list = new ArrayList<>();
		Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
			int displayIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
			int dateAddedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
			int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
			int typeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);

			do {
				ImageHolder holder = new ImageHolder(
						cursor.getInt(idIndex),
						cursor.getString(displayIndex),
						cursor.getString(typeIndex),
						cursor.getLong(dateAddedIndex),
						cursor.getLong(sizeIndex),
						Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(idIndex)));

				holder.dateTakenString = String.valueOf(AppUtils.formatDateTime(getContext(), holder.date * 1000));

				list.add(holder);
			}
			while (cursor.moveToNext());

			Collections.sort(list, getDefaultComparator());
		}

		cursor.close();

		return list;
	}

	@NonNull
	@Override
	public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		return new RecyclerViewAdapter.ViewHolder(getInflater()
				.inflate(isGridLayoutRequested() ? R.layout.list_image_grid : R.layout.list_image, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position)
	{
		final View parentView = holder.getView();
		final ImageHolder object = getItem(position);

		final View selector = parentView.findViewById(R.id.selector);
		final View layoutImage = parentView.findViewById(R.id.layout_image);
		ImageView image = parentView.findViewById(R.id.image);
		TextView text1 = parentView.findViewById(R.id.text);
		TextView text2 = parentView.findViewById(R.id.text2);

		text1.setText(object.friendlyName);
		text2.setText(object.dateTakenString);

		if (getSelectionConnection() != null)
			selector.setSelected(object.isSelectableSelected());

		SweetImageLoader.load(this, getContext(), image, object);
	}

	@Override
	public boolean isGridSupported()
	{
		return true;
	}

	public static class ImageHolder extends Shareable
	{
		public long id;
		public String dateTakenString;

		public ImageHolder(int id, String filename, String mimeType, long date, long size, Uri uri)
		{
			super(filename, filename, mimeType, date, size, uri);
			this.id = id;
		}
	}
}
