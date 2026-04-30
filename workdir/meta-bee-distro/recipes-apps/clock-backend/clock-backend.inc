# Custom overrides for clock-backend
# This file is included by the auto-generated clock-backend_master.bb

inherit systemd

# Required for alsa-sys build script to find ALSA headers
DEPENDS += "alsa-lib pkgconfig-native"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=d41d8cd98f00b204e9800998ecf8427e"

SRC_URI += "file://clock-backend.service"

# Enable hardware paths for the target device
CARGO_FEATURES = "target-platform"
CARGO_BUILD_FLAGS += "--features '${CARGO_FEATURES}'"

# Runtime directory and working directory for the service
BACKEND_DATADIR = "/usr/share/bee/clock-backend"

SYSTEMD_SERVICE:${PN} = "clock-backend.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

FILES:${PN} += " \
    ${BACKEND_DATADIR} \
    ${systemd_unitdir}/system/clock-backend.service \
"

do_install:append() {
    # Install binary
    install -d ${D}${bindir}
    install -m 0755 ${B}/target/${CARGO_TARGET_SUBDIR}/clock-backend ${D}${bindir}/clock-backend

    # Install working directory with default configuration
    install -d ${D}${BACKEND_DATADIR}
    install -m 0644 ${S}/default_configuration.json ${D}${BACKEND_DATADIR}/default_configuration.json

    # Media directory served by the backend over HTTP
    install -d ${D}${BACKEND_DATADIR}/media

    # Install systemd unit
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/clock-backend.service ${D}${systemd_unitdir}/system/clock-backend.service
}
