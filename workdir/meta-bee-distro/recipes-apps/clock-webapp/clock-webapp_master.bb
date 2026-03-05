DESCRIPTION = "Clock webapp (React/Vite) served by lighttpd"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${WORKDIR}/git/LICENSE;md5=d41d8cd98f00b204e9800998ecf8427e"

inherit systemd

DEFAULT_PREFERENCE = "-1"

PVBASE := "${PV}"

BRANCH = "${PVBASE}"
TAG = "${PVBASE}"
SRC_URI = "\
    git://github.com/beestarbush/clock-webapp.git;branch=${BRANCH};tag=${TAG};protocol=https \
    file://clock-webapp.conf \
"

# NOTE: The webapp must be pre-built before committing.
#   cd webapp && npm install && npm run build
#   Commit the dist/ directory to the repository.

S = "${WORKDIR}/git"

WEBAPP_DATADIR = "/usr/share/bee/webapp"

RDEPENDS:${PN} += " \
    lighttpd \
    clock-backend \
"

FILES:${PN} += " \
    ${WEBAPP_DATADIR} \
    ${sysconfdir}/lighttpd.d/clock-webapp.conf \
"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${WEBAPP_DATADIR}
    cp -r ${S}/dist/. ${D}${WEBAPP_DATADIR}/

    install -d ${D}${sysconfdir}/lighttpd.d
    install -m 0644 ${WORKDIR}/clock-webapp.conf ${D}${sysconfdir}/lighttpd.d/clock-webapp.conf
}
