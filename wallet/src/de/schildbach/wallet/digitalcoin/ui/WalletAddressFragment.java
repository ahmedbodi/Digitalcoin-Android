/*
 * Copyright 2011-2013 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.digitalcoin.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.digitalcoin.core.Address;
import com.google.digitalcoin.uri.DigitalcoinURI;

import de.schildbach.wallet.digitalcoin.Constants;
import de.schildbach.wallet.digitalcoin.WalletApplication;
import de.schildbach.wallet.digitalcoin.util.BitmapFragment;
import de.schildbach.wallet.digitalcoin.util.NfcTools;
import de.schildbach.wallet.digitalcoin.util.WalletUtils;
import de.schildbach.wallet.digitalcoin.R;

/**
 * @author Andreas Schildbach
 */
public final class WalletAddressFragment extends Fragment
{
	private FragmentActivity activity;
	private WalletApplication application;
	private SharedPreferences prefs;
	private Object nfcManager;

	private View digitalcoinAddressButton;
	private TextView digitalcoinAddressLabel;
	private ImageView digitalcoinAddressQrView;

	private Address lastSelectedAddress;

	private Bitmap qrCodeBitmap;

	@Override
	public void onAttach(final Activity activity)
	{
		super.onAttach(activity);

		this.activity = (FragmentActivity) activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		application = (WalletApplication) activity.getApplication();
	}

	@SuppressLint("InlinedApi")
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		nfcManager = activity.getSystemService(Context.NFC_SERVICE);

		final View view = inflater.inflate(R.layout.wallet_address_fragment, container, false);
		digitalcoinAddressButton = view.findViewById(R.id.digitalcoin_address_button);
		digitalcoinAddressLabel = (TextView) view.findViewById(R.id.digitalcoin_address_label);
		digitalcoinAddressQrView = (ImageView) view.findViewById(R.id.digitalcoin_address_qr);

		digitalcoinAddressButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(final View v)
			{
				AddressBookActivity.start(activity, false);
			}
		});

		digitalcoinAddressQrView.setOnClickListener(new OnClickListener()
		{
			public void onClick(final View v)
			{
				handleShowQRCode();
			}
		});

		digitalcoinAddressQrView.setOnLongClickListener(new OnLongClickListener()
		{
			public boolean onLongClick(final View v)
			{
				startActivity(new Intent(activity, RequestCoinsActivity.class));
				return true;
			}
		});

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		prefs.registerOnSharedPreferenceChangeListener(prefsListener);

		updateView();
	}

	@Override
	public void onPause()
	{
		prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);

		if (nfcManager != null)
			NfcTools.unpublish(nfcManager, getActivity());

		super.onPause();
	}

	private void updateView()
	{
		final Address selectedAddress = application.determineSelectedAddress();

		if (!selectedAddress.equals(lastSelectedAddress))
		{
			lastSelectedAddress = selectedAddress;

			digitalcoinAddressLabel.setText(WalletUtils.formatAddress(selectedAddress, Constants.ADDRESS_FORMAT_GROUP_SIZE,
					Constants.ADDRESS_FORMAT_LINE_SIZE));

			final String addressStr = DigitalcoinURI.convertToDigitalcoinURI(selectedAddress, null, null, null);

			final int size = (int) (256 * getResources().getDisplayMetrics().density);
			qrCodeBitmap = WalletUtils.getQRCodeBitmap(addressStr, size);
			digitalcoinAddressQrView.setImageBitmap(qrCodeBitmap);

			if (nfcManager != null)
				NfcTools.publishUri(nfcManager, getActivity(), addressStr);
		}
	}

	private void handleShowQRCode()
	{
		BitmapFragment.show(getFragmentManager(), qrCodeBitmap);
	}

	private final OnSharedPreferenceChangeListener prefsListener = new OnSharedPreferenceChangeListener()
	{
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key)
		{
			if (Constants.PREFS_KEY_SELECTED_ADDRESS.equals(key))
				updateView();
		}
	};
}
