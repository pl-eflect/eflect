import os

import numpy as np
import pandas as pd

def load_footprint(path):
    return pd.read_csv(path, parse_dates = ['start', 'end'])

def footprint_to_power_summary(footprint):
    df = footprint.groupby(['start', 'end', 'domain']).agg({'app_energy': 'sum', 'total_energy': 'max'}).reset_index()

    timestamps = pd.date_range(df.start.min().floor('s'), df.end.max().ceil('s'), freq = '40ms', closed = None)
    elapsed = df.end - df.start
    elapsed = elapsed.dt.seconds + elapsed.dt.microseconds / 1000000

    df['app_power'] = df.app_energy / elapsed
    df['total_power'] = df.total_energy / elapsed
    df['other_power'] = df.total_power - df.app_power

    df = pd.concat([df, pd.DataFrame(data = {'start': timestamps})]).set_index(['start', 'domain'])
    df = df[['app_power', 'other_power', 'total_power']].clip(0).unstack().bfill().dropna(axis = 1, thresh = 5)
    df = df.reindex(timestamps).rolling(250).mean().dropna()
    df.index.name = 'time'
    df = df.stack(0)
    df.columns = [int(domain) for domain in df.columns]
    df.columns.name = 'domain'
    df = df.unstack().stack(0)

    return df

def power_summary(root, warm_up_frac = 5):
    iters = np.sort(os.listdir(root))
    iters = iters[(len(iters) // 5):]
    footprints = [load_footprint(os.path.join(root, f)) for f in iters]
    return pd.concat([footprint_to_power_summary(f) for f in footprints if len(f) > 0])

def process_coruns(root):
    return pd.concat([power_summary(os.path.join(root, run)).assign(run = run) for run in os.listdir(root)])
