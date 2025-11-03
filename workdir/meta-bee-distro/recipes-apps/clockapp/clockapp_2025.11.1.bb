DESCRIPTION = "Clock application"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${WORKDIR}/git/LICENSE;md5=d41d8cd98f00b204e9800998ecf8427e"

inherit qt6-cmake qt6-paths systemd

DEFAULT_PREFERENCE="-1"

PVBASE := "${PV}"

BRANCH_PATH = ""
BRANCH = "2025.11"
TAG = "2025.11.1"
SRC_URI = "\
    git://github.com/beestarbush/ClockApp.git;branch=${BRANCH};tag=${TAG};protocol=https \
    file://clockapp.service \
"

S = "${WORKDIR}/git/src"

# This is a Release build
EXTRA_OECMAKE = "-DCMAKE_BUILD_TYPE=Release"

DEPENDS += " \
    qtquick3d \ 
    libgpiod \
    qtbase \
    qtmultimedia \
    openssl \
    qtbase-native \
    qtquick3d \
    qtquick3d-native \
    qtnetworkauth \
"

RDEPENDS:${PN} = " \
    fontconfig \
    libgpiod-tools \
    qtbase \
    qtbase-plugins \
    qtdeclarative-qmlplugins \
    qtserialport \
    qtsvg \
    qtsvg-plugins \
    openssl \
    qtmultimedia \
    qtmultimedia-plugins \
    qtquick3d-plugins \
    qtquick3d-qmlplugins \
"

SOLIBS = ".so"
SOLIBSDEV = ".so.*"
FILES:${PN} += " \
    /usr/lib/systemd \
    /etc/systemd/system \
    /usr/share/bee \
    ${libdir}/lib*${SOLIBS} \
"
FILES_SOLIBSDEV = ""
FILES:${PN}-dev = " \
    ${FILES_SOLIBSDEV} \
"

SYSTEMD_SERVICE:${PN} = "clockapp.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# Ability to find GIT and PkgConfig of host instead of target only.
OECMAKE_FIND_ROOT_PATH_MODE_PROGRAM = "BOTH"

do_configure:prepend() {
    OUTFILE="${S}/git_version.h"

    cd ${S}

    GIT_TAG=$(git describe --tags --always --dirty 2>/dev/null || echo "unknown")
    GIT_COMMIT_HASH=$(git rev-parse HEAD 2>/dev/null || echo "unknown")
    GIT_COMMIT_HASH_SHORT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    GIT_DIRTY=$(git diff --quiet || echo "-dirty")

    cat > "$OUTFILE" <<EOF
#pragma once
#define GIT_TAG "${GIT_TAG}"
#define GIT_COMMIT_HASH "${GIT_COMMIT_HASH}"
#define GIT_COMMIT_HASH_SHORT "${GIT_COMMIT_HASH_SHORT}"
#define GIT_DIRTY "${GIT_DIRTY}"
EOF

    cd -
}

do_install:append () {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/clockapp.service ${D}${systemd_unitdir}/system

    install -d ${D}/usr/share/bee/media
}
