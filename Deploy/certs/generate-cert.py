from pathlib import Path
from datetime import datetime, timedelta, timezone
from ipaddress import ip_address
from cryptography import x509
from cryptography.x509.oid import NameOID
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa

base = Path(r'c:\Users\riose\OneDrive\Escritorio\Tingeso\Entrega 1\Tingeso-1\Deploy\certs')
key_path = base / 'server.key'
cert_path = base / 'server.crt'

key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
subject = issuer = x509.Name([
    x509.NameAttribute(NameOID.COUNTRY_NAME, 'CL'),
    x509.NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, 'Region Metropolitana'),
    x509.NameAttribute(NameOID.LOCALITY_NAME, 'Santiago'),
    x509.NameAttribute(NameOID.ORGANIZATION_NAME, 'TravelAgency'),
    x509.NameAttribute(NameOID.ORGANIZATIONAL_UNIT_NAME, 'Dev'),
    x509.NameAttribute(NameOID.COMMON_NAME, '3.22.168.224'),
])

san = x509.SubjectAlternativeName([
    x509.IPAddress(ip_address('3.22.168.224')),
    x509.IPAddress(ip_address('127.0.0.1')),
    x509.DNSName('localhost'),
])

cert = (
    x509.CertificateBuilder()
    .subject_name(subject)
    .issuer_name(issuer)
    .public_key(key.public_key())
    .serial_number(x509.random_serial_number())
    .not_valid_before(datetime.now(timezone.utc) - timedelta(minutes=5))
    .not_valid_after(datetime.now(timezone.utc) + timedelta(days=365))
    .add_extension(san, critical=False)
    .add_extension(x509.BasicConstraints(ca=False, path_length=None), critical=True)
    .add_extension(x509.KeyUsage(digital_signature=True, content_commitment=False, key_encipherment=True, data_encipherment=True, key_agreement=False, key_cert_sign=False, crl_sign=False, encipher_only=False, decipher_only=False), critical=True)
    .add_extension(x509.ExtendedKeyUsage([x509.oid.ExtendedKeyUsageOID.SERVER_AUTH]), critical=False)
    .sign(private_key=key, algorithm=hashes.SHA256())
)

key_path.write_bytes(key.private_bytes(encoding=serialization.Encoding.PEM, format=serialization.PrivateFormat.TraditionalOpenSSL, encryption_algorithm=serialization.NoEncryption()))
cert_path.write_bytes(cert.public_bytes(serialization.Encoding.PEM))
