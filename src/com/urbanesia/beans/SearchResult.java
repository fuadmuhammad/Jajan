package com.urbanesia.beans;

import android.text.Html;
import android.view.View;

public class SearchResult {
	private String 
		businessUri = "", businessPhoto = "", businessParentName = "",
		businessParentUri = "", businessDistance = "", businessReviewCount = "",
		businessReview = "", businessShortLink = "", businessMainCat = "", businessSubcat = "",
		businessRating = "", businessTwitterName = "", businessTelephone = "", businessWebsite = "",
		businessName = "", businessAddress1 = "", businessAddress2 = "",
		businessKelurahan = "", businessKecamatan = "", businessKabupaten = "",
		businessProvinsi = "", businessKodePos = "", businessLatitude = "", businessLongitude = "",
		businessMobileWebURL = "";
	private boolean
		isUrspot = false, isPanoramic = false;
	private int position = 0;
	private View v;

	public void setBusinessAddress1(String businessAddress1) {
		this.businessAddress1 = businessAddress1;
	}

	public String getBusinessAddress1() {
		return businessAddress1;
	}

	public void setBusinessAddress2(String businessAddress2) {
		this.businessAddress2 = businessAddress2;
	}

	public String getBusinessAddress2() {
		return businessAddress2;
	}

	public void setBusinessName(String businessName) {
		this.businessName = Html.fromHtml(businessName).toString();
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessKabupaten(String businessKabupaten) {
		this.businessKabupaten = businessKabupaten;
	}

	public String getBusinessKabupaten() {
		return businessKabupaten;
	}

	public void setBusinessProvinsi(String businessProvinsi) {
		this.businessProvinsi = businessProvinsi;
	}

	public String getBusinessProvinsi() {
		return businessProvinsi;
	}

	public void setBusinessKecamatan(String businessKecamatan) {
		this.businessKecamatan = businessKecamatan;
	}

	public String getBusinessKecamatan() {
		return businessKecamatan;
	}

	public void setBusinessKelurahan(String businessKelurahan) {
		this.businessKelurahan = businessKelurahan;
	}

	public String getBusinessKelurahan() {
		return businessKelurahan;
	}

	public void setBusinessPhoto(String businessPhoto) {
		this.businessPhoto = businessPhoto;
	}

	public String getBusinessPhoto() {
		return businessPhoto;
	}

	public void setBusinessUri(String businessUri) {
		this.businessUri = businessUri;
	}

	public String getBusinessUri() {
		return businessUri;
	}

	public void setBusinessParentName(String businessParentName) {
		this.businessParentName = businessParentName;
	}

	public String getBusinessParentName() {
		return businessParentName;
	}

	public void setBusinessReviewCount(String businessReviewCount) {
		this.businessReviewCount = businessReviewCount;
	}

	public String getBusinessReviewCount() {
		return businessReviewCount;
	}

	public void setBusinessDistance(String businessDistance) {
		this.businessDistance = businessDistance;
	}

	public String getBusinessDistance() {
		return businessDistance;
	}

	public void setBusinessParentUri(String businessParentUri) {
		this.businessParentUri = businessParentUri;
	}

	public String getBusinessParentUri() {
		return businessParentUri;
	}

	public void setBusinessMainCat(String businessMainCat) {
		this.businessMainCat = businessMainCat;
	}

	public String getBusinessMainCat() {
		return businessMainCat;
	}

	public void setBusinessReview(String businessReview) {
		this.businessReview = Html.fromHtml(businessReview).toString();
	}

	public String getBusinessReview() {
		return businessReview;
	}

	public void setBusinessShortLink(String businessShortLink) {
		this.businessShortLink = businessShortLink;
	}

	public String getBusinessShortLink() {
		return businessShortLink;
	}

	public void setBusinessSubcat(String businessSubcat) {
		this.businessSubcat = businessSubcat;
	}

	public String getBusinessSubcat() {
		return businessSubcat;
	}

	public void setBusinessWebsite(String businessWebsite) {
		this.businessWebsite = businessWebsite;
	}

	public String getBusinessWebsite() {
		return businessWebsite;
	}

	public void setBusinessTwitterName(String businessTwitterName) {
		this.businessTwitterName = businessTwitterName;
	}

	public String getBusinessTwitterName() {
		return businessTwitterName;
	}

	public void setBusinessRating(String businessRating) {
		this.businessRating = businessRating;
	}

	public String getBusinessRating() {
		return businessRating;
	}

	public void setBusinessTelephone(String businessTelephone) {
		this.businessTelephone = businessTelephone;
	}

	public String getBusinessTelephone() {
		return businessTelephone;
	}

	public void setBusinessKodePos(String businessKodePos) {
		this.businessKodePos = businessKodePos;
	}

	public String getBusinessKodePos() {
		return businessKodePos;
	}

	public void setBusinessLongitude(String businessLongitude) {
		this.businessLongitude = businessLongitude;
	}

	public String getBusinessLongitude() {
		return businessLongitude;
	}

	public void setBusinessLatitude(String businessLatitude) {
		this.businessLatitude = businessLatitude;
	}

	public String getBusinessLatitude() {
		return businessLatitude;
	}

	public void setBusinessMobileWebURL(String businessMobileWebURL) {
		this.businessMobileWebURL = businessMobileWebURL;
	}

	public String getBusinessMobileWebURL() {
		return businessMobileWebURL;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public void setView(View v) {
		this.v = v;
	}

	public View getView() {
		return v;
	}

	public void setUrspot(boolean isUrspot) {
		this.isUrspot = isUrspot;
	}

	public boolean isUrspot() {
		return isUrspot;
	}

	public void setPanoramic(boolean isPanoramic) {
		this.isPanoramic = isPanoramic;
	}

	public boolean isPanoramic() {
		return isPanoramic;
	}
}