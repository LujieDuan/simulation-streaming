#https://pythonprogramming.net/vehicle-data-visualization-application-dash-python-tutorial/?completed=/live-graphs-data-visualization-application-dash-python-tutorial/


import dash
import dash_core_components as dcc
import dash_html_components as html
import time
from collections import deque
import plotly.graph_objs as go
import random
import redis

app = dash.Dash('streaming-data-web-viz')

r = redis.Redis(host='localhost', port=6379, db=0)
p = r.pubsub(ignore_subscribe_messages=True)

max_queue_length = 5000

channels = {}
channel_times = {}

def get_channels():
    # PUBSUB CHANNELS [pattern]
    pass


def sub_to_channel(chan):
    # Thread functions
    # sub to channel and added to thread safe queue
    p.subscribe(ch)

    new_ch = deque(maxlen=max_queue_length)
    new_time = deque(maxlen=max_queue_length)
    channels[chan] = new_ch
    channel_times[chan] = new_time

channels_name = ["Royal_University_Hospital_patient",
                            "Saskatoon_City_Hospital_patient",
                            "St_Pauls_Hospital_patient"]

for ch in channels_name:
    sub_to_channel(ch)


###### HTML 
app.layout = html.Div([
    html.Div([
        html.H2('Streaming Data Web Visualization - Live',
                style={'float': 'center',
                       }),
        ]),
    html.Div(children=html.Div(id='graphs'), className='row'),
    dcc.Interval(
        id='graph-update',
        interval=1000),
    ], className="container",style={'width':'98%','margin-left':10,'margin-right':10,'max-width':50000})



# Callback to update the plots based on available data points.
@app.callback(
    dash.dependencies.Output('graphs','children'),
    events=[dash.dependencies.Event('graph-update', 'interval')]
    )
def update_graph():
    message = p.get_message()
    if message:
        while message: 
            legnth = len(channel_times[message['channel'].decode()])
            channel_times[message['channel'].decode()].append(legnth+1)#time.time())
            print(message)
            channels[message['channel'].decode()].append(float(message['data'].decode()))
            message = p.get_message()
    else: 
        pass
    graphs = []
    if len(channels)>2:
        class_choice = 'col s12 m6 l4'
    elif len(channels) == 2:
        class_choice = 'col s12 m6 l6'
    else:
        class_choice = 'col s12'

    for ch in channels:

        data = go.Scatter(
            x=list(channel_times[ch]),
            y=list(channels[ch]),
            name='Scatter',
            fill="tozeroy",
            fillcolor="#6897bb"
            )

        if (len(channel_times[ch]) > 0):
             graphs.append(html.Div(dcc.Graph(
                id=ch,
                animate=True,
                figure={'data': [data],'layout' : go.Layout(xaxis=dict(range=[min(channel_times[ch]) - 1,max(channel_times[ch]) + 1]),
                                                            yaxis=dict(range=[min(channels[ch]) - 1,max(channels[ch]) + 1]), 
                                                            title='{}'.format(ch))}
            ), className=class_choice))
        else:
            graphs.append(html.Div(dcc.Graph(
                id=ch,
                animate=True,
                figure={'data': [data],'layout' : go.Layout(title='{}'.format(ch))}
                ), className=class_choice))

    return graphs


# CSS and JS
external_css = ["https://cdnjs.cloudflare.com/ajax/libs/materialize/0.100.2/css/materialize.min.css"]
for css in external_css:
    app.css.append_css({"external_url": css})

external_js = ['https://cdnjs.cloudflare.com/ajax/libs/materialize/0.100.2/js/materialize.min.js']
for js in external_css:
    app.scripts.append_script({'external_url': js})

# Start the Dash App
if __name__ == '__main__':
    app.run_server(debug=True)