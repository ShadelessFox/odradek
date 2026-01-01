package sh.adelessfox.odradek.audio.libatrac9;

enum Atrac9Status {
    ERR_SUCCESS(0),
    ERR_NOT_IMPLEMENTED(0x80000000),
    ERR_BAD_CONFIG_DATA(0x81000000),
    ERR_UNPACK_SUPERFRAME_FLAG_INVALID(0x82000000),
    ERR_UNPACK_REUSE_BAND_PARAMS_INVALID(0x82000001),
    ERR_UNPACK_BAND_PARAMS_INVALID(0x82000002),
    ERR_UNPACK_GRAD_BOUNDARY_INVALID(0x82100000),
    ERR_UNPACK_GRAD_START_UNIT_OOB(0x82100001),
    ERR_UNPACK_GRAD_END_UNIT_OOB(0x82100002),
    ERR_UNPACK_GRAD_START_VALUE_OOB(0x82100003),
    ERR_UNPACK_GRAD_END_VALUE_OOB(0x82100004),
    ERR_UNPACK_GRAD_END_UNIT_INVALID(0x82100005), // start_unit > end_unit
    ERR_UNPACK_SCALE_FACTOR_MODE_INVALID(0x82100006),
    ERR_UNPACK_SCALE_FACTOR_OOB(0x82100007),
    ERR_UNPACK_EXTENSION_DATA_INVALID(0x82100008);

    final int value;

    Atrac9Status(int value) {
        this.value = value;
    }

    static Atrac9Status valueOf(int value) {
        for (Atrac9Status status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status value: " + value);
    }
}
