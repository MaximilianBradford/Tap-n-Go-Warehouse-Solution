# Generated by Django 3.2.4 on 2021-07-01 15:39

from django.db import migrations
from common.models import InvenTreeSetting
from InvenTree.config import get_setting

def set_default_currency(apps, schema_editor):
    """ migrate the currency setting from config.yml to db """
    # get value from settings-file
    base_currency = get_setting('INVENTREE_BASE_CURRENCY', 'base_currency', 'USD')

    from common.currency import currency_codes

    # check if value is valid
    if base_currency not in currency_codes():
        if len (currency_codes()) > 0:
            base_currency = currency_codes()[0]
        else:
            base_currency = 'USD'

    # write to database
    InvenTreeSetting.set_setting('INVENTREE_DEFAULT_CURRENCY', base_currency, None, create=True)


class Migration(migrations.Migration):

    dependencies = [
        ('common', '0009_delete_currency'),
    ]

    operations = [
        migrations.RunPython(set_default_currency, reverse_code=migrations.RunPython.noop),
    ]
