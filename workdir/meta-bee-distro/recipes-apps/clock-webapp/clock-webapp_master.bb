DESCRIPTION = "Clock webapp (React/Vite) served by lighttpd"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${WORKDIR}/git/LICENSE;md5=d41d8cd98f00b204e9800998ecf8427e"

inherit npm systemd

DEFAULT_PREFERENCE = "-1"

PVBASE := "${PV}"

BRANCH = "${PVBASE}"
TAG = "${PVBASE}"
SRC_URI = "\
    git://github.com/beestarbush/clock-webapp.git;branch=${BRANCH};tag=${TAG};protocol=https \
    npmsw://${THISDIR}/files/npm-shrinkwrap.json \
    file://clock-webapp.conf \
"

# NOTE: Regenerate files/npm-shrinkwrap.json whenever webapp/package.json changes:
#   cd webapp && npm install && npm shrinkwrap
#   cp npm-shrinkwrap.json <recipe-dir>/files/

S = "${WORKDIR}/git/webapp"

WEBAPP_DATADIR = "/usr/share/bee/webapp"

DEPENDS += "nodejs-native"

RDEPENDS:${PN} += " \
    lighttpd \
    clock-backend \
"

FILES:${PN} += " \
    ${WEBAPP_DATADIR} \
    ${sysconfdir}/lighttpd/conf.d/clock-webapp.conf \
"

NPM_FLAGS = "--ignore-scripts"

do_compile() {
    cd ${S}
    npm run build
}

do_install() {
    install -d ${D}${WEBAPP_DATADIR}
    cp -r ${S}/dist/. ${D}${WEBAPP_DATADIR}/

    install -d ${D}${sysconfdir}/lighttpd/conf.d
    install -m 0644 ${WORKDIR}/clock-webapp.conf ${D}${sysconfdir}/lighttpd/conf.d/clock-webapp.conf
}
