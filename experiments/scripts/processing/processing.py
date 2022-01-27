import os

import numpy as np
import pandas as pd

def load_footprint(path):
    return pd.read_csv(path, parse_dates = ['start', 'end'])

# evaluation processing
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
    df = df.reindex(timestamps).rolling(25).mean().fillna(0)
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

# chappie processing
def filter_to_app(trace):
    trace = trace.split(';')
    while len(trace) > 0:
        if r'.' not in trace[0] or '_' in trace[0] or '::' in trace[0] or r'java' in trace[0] or 'jdk' in trace[0] or 'eflect' in trace[0] or 'chappie' in trace[0] or r'.so' in trace[0]:
            trace.pop()
        else:
            return trace[0]

def footprint_to_ranking(footprint):
    df = footprint.dropna(subset=['trace']).groupby(['start', 'end']).agg({'app_energy': 'sum', 'trace': list}).reset_index()

    timestamps = pd.date_range(df.start.min().floor('s'), df.end.max().ceil('s'), freq = '40ms', closed = None)
    elapsed = df.end - df.start
    elapsed = elapsed.dt.seconds + elapsed.dt.microseconds / 1000000

    df['app_power'] = df.app_energy / elapsed / df.trace.map(len)

    df = pd.concat([df, pd.DataFrame(data = {'start': timestamps})]).set_index('start').dropna().explode('trace').groupby('trace').sum()
    df = df.app_power.clip(0)
    df = df.rolling(25).mean().fillna(0)
    df = df / df.sum()
    df.index.name = 'trace'
    df.name = 'energy'
    df.index = df.index.map(filter_to_app)
    df = df.groupby('trace').sum().sort_values()

    return df

def ranking_summary(root, warm_up_frac = 5):
    iters = np.sort(os.listdir(root))
    iters = iters[(len(iters) // 5):]
    footprints = [load_footprint(os.path.join(root, f)) for f in iters]
    return pd.concat([footprint_to_ranking(f) for f in footprints if len(f) > 0])

def process_chappie_coruns(root):
    return pd.concat([ranking_summary(os.path.join(root, run)).to_frame().assign(run = run) for run in os.listdir(root)])

# aeneas processing
def footprint_to_aeneas_summary(footprint):
    df = footprint.copy()
    df = df[df.start != '+1000000000-12-31T23:59:59.999999999Z']
    df.start = pd.to_datetime(df.start)
    df = df.set_index('start').app_energy

    df = df.dropna()
    df = df.rolling(25).mean().fillna(0)
    df = df.sort_index()
    df.index.name = 'time'
    df.name = 'energy'

    return df

def aeneas_power_summary(root, warm_up_frac = 5):
    iters = np.sort(os.listdir(root))
    iters = iters[(len(iters) // 5):]
    footprints = [load_footprint(os.path.join(root, f)) for f in iters]
    return pd.concat([footprint_to_aeneas_summary(f) for f in footprints if len(f) > 0])

def process_aeneas_coruns(root):
    return pd.concat([aeneas_power_summary(os.path.join(root, run)).to_frame().assign(run = run) for run in os.listdir(root)])
