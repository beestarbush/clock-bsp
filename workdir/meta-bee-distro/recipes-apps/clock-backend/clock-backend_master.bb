DESCRIPTION = "Clock backend"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${WORKDIR}/git/LICENSE;md5=d41d8cd98f00b204e9800998ecf8427e"

inherit cargo systemd

DEFAULT_PREFERENCE = "-1"

PVBASE := "${PV}"

BRANCH = "${PVBASE}"
TAG = "${PVBASE}"
SRC_URI = "\
    git://github.com/beestarbush/clock-backend.git;branch=${BRANCH};tag=${TAG};protocol=https \
    file://clock-backend.service \
    crate://crates.io/axum/0.7.9 \
    crate://crates.io/tokio/1.36.0 \
    crate://crates.io/serde/1.0.218 \
    crate://crates.io/serde_json/1.0.140 \
    crate://crates.io/tower-http/0.5.2 \
"

S = "${WORKDIR}/git/backend"

CARGO_SRC_DIR = ""

# Enable hardware paths for the target device
CARGO_FEATURES = "target-platform"

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
    install -m 0644 ${S}/configuration.json ${D}${BACKEND_DATADIR}/configuration.json

    # Media directory served by the backend over HTTP
    install -d ${D}${BACKEND_DATADIR}/media

    # Install systemd unit
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/clock-backend.service ${D}${systemd_unitdir}/system/clock-backend.service
}
