package com.henheang.hphsar.common;

/**
 * Centralized exception message literals that were duplicated across service classes.
 * Each constant here replaced 2+ identical throw new XException("...") call sites
 * with the exact same text and exception type; no wording changed.
 */
public final class ExceptionMessages {

    private ExceptionMessages() {
    }

    // ─── BadRequestException ───
    public static final String CAN_NOT_USE_DEFAULT_VALUE_PLEASE_INPUT_VALUE = "Can not use default value. Please input value! ";
    public static final String DESCRIPTION_WORD_COUNT_CAN_NOT_EXCEED_100 = "Description word count can not exceed 100.";
    public static final String FIELD_SORT_IS_INVALID_PLEASE_INPUT_EITHER_ASC = "Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.";
    public static final String ADDITIONAL_PHONE_FORMAT_INVALID = "Incorrect additional phone number format. Phone number should start with '0', 8 or 9 index, and can not be '0' on second index. Example: 0XXXXXXXX(X).";
    public static final String PRIMARY_PHONE_FORMAT_INVALID = "Incorrect primary phone number format. Phone number should start with '0', 8 or 9 index, and can not be '0' on second index. Example: 0XXXXXXXX(X).";
    public static final String INTEGER_OVERFLOW = "Integer value can not exceed 2147483646";
    public static final String INVALID_INPUT_AVAILABLE_SORTING_ARE_CREATED_DATE_IS = "Invalid input. Available sorting are 'created_date' - 'is_publish' - 'name'. Case sensitive not needed.";
    public static final String INACTIVE_IS_REQUIRED = "isActive is required.";
    public static final String ONE_OF_THE_FIELDS_INSIDE_THE_STOREREQUEST_OBJECT = "One of the fields inside the StoreRequest object is null.";
    public static final String ADDITIONAL_PHONE_PREFIX_INVALID = "Opps, please input the valid additional phone number start with ( 0 ) or (855)";
    public static final String OUT_OF_RANGE_RATING_RANGE_IS_FROM_1 = "Out of range. Rating range is from 1 to 5.";
    public static final String PAGE_SIZE_MUST_BE_POSITIVE = "Page number and size should be higher than 0.";
    public static final String THIS_USER_DOES_NOT_EXIST = "This user does not exist.";

    // ─── ConflictException ───
    public static final String NOT_ENOUGH_PRODUCT_IN_STOCK = "Not enough product in stock.";

    // ─── DefaultValueException ───
    public static final String CAN_NOT_USE_DEFAULT_VALUE_PLEASE_INPUT_LEGAL = "Can not use default value. Please input legal value.";

    // ─── InternalServerErrorException ───
    public static final String FAIL_TO_CHANGE_STATUS = "Fail to change status";
    public static final String FAIL_TO_CREATE_NOTIFICATION = "Fail to create notification.";
    public static final String FAIL_TO_FETCH_ORDER_DETAILS = "Fail to fetch order details.";
    public static final String FAIL_TO_FETCH_ORDER_INVOICE = "Fail to fetch order invoice.";
    public static final String SOMETHING_WENT_WRONG_WHILE_DELETING = "Something went wrong while deleting";
    public static final String SOMETHING_WENT_WRONG_WHILE_DOING_BOOKMARK_OPERATION = "Something went wrong while doing bookmark operation";

    // ─── NotFoundException ───
    public static final String BUYER_NOT_FOUND = "Buyer not found.";
    public static final String CART_DOES_NOT_EXIST = "Cart does not exist.";
    public static final String DRAFT_NOT_FOUND = "Draft not found.";
    public static final String NO_CART_IS_FOUND = "No cart is found.";
    public static final String NOTIFICATION_NOT_FOUND = "Notification not found.";
    /** Canonical wording for "the requested order(s) could not be found" — merged from 3 near-duplicate phrasings (with/without trailing period). */
    public static final String ORDER_NOT_FOUND = "Order not found.";
    /** Canonical wording for "the requested store could not be found" — also merged from 3 call sites that incorrectly threw ConflictException (409) for this same "resource missing" condition; corrected to NotFoundException (404) to match the read path. */
    public static final String STORE_NOT_FOUND = "Store not found.";
    /** Canonical wording for "this search/listing returned no stores" — merged from 2 near-duplicate phrasings. Distinct from {@link #USER_HAVE_NOT_CREATED_STORE}, which is about the current supplier's own store. */
    public static final String STORES_NOT_FOUND = "Stores not found.";
    public static final String SUPPLIER_NOT_FOUND = "Supplier not found.";
    public static final String THERE_IS_NO_ORDER_CURRENTLY = "There is no order currently.";
    public static final String THIS_NOTIFICATION_DOES_NOT_EXIST = "This notification does not exist.";
    public static final String THIS_ORDER_IS_NOT_PENDING = "This order is not pending";
    public static final String THIS_PRODUCT_IS_NOT_FOUND_IN_THIS_STORE = "This product is not found in this store.";
    public static final String THIS_STORE_ID_DOES_NOT_EXIST = "This store id does not exist.";
    public static final String USER_HAVE_NOT_CREATED_PROFILE_YET_PLEASE_CREATE = "User have not created profile yet. Please create user profile to use this feature.";
    /** Canonical wording for "the current supplier hasn't created their store yet" — merged from 4 near-duplicate phrasings. Distinct from {@link #STORES_NOT_FOUND}. */
    public static final String USER_HAVE_NOT_CREATED_STORE = "User have not created store. Please create store.";
    public static final String USER_NOT_FOUND_INVALID_EMAIL = "User not found. Invalid email.";
    public static final String YOU_HAVE_NO_UNREAD_NOTIFICATION = "You have no unread notification.";

    // ─── OKException ───
    public static final String PRODUCTS_NOT_FOUND = "Products not found.";
}
